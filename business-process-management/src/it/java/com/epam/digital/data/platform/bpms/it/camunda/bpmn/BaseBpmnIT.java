package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.managementService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.it.BaseIT;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseBpmnIT extends BaseIT {

  private static final String MOCK_SERVER = "/mock-server";
  private static final String SETTINGS_MOCK_SERVER = "/user-settings-mock-server";
  protected static final String EXCERPT_SERVICE_MOCK_SERVER = "/excerpt-mock-service";
  public static final String START_FORM_CEPH_KEY = "startFormCephKey";

  @Inject
  @Qualifier("digitalSignatureMockServer")
  protected WireMockServer digitalSignatureMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  protected WireMockServer dataFactoryMockServer;
  @Inject
  @Qualifier("userSettingsWireMock")
  protected WireMockServer userSettingsWireMock;
  @Inject
  @Qualifier("excerptServiceWireMock")
  protected WireMockServer excerptServiceWireMock;
  @Inject
  @Qualifier("trembitaMockServer")
  protected WireMockServer trembitaMockServer;
  @Value("${ceph.bucket}")
  protected String cephBucketName;

  @Inject
  protected ObjectMapper objectMapper;
  @Inject
  protected CephKeyProvider cephKeyProvider;

  protected String testUserName = "testuser";
  protected String testUserToken;

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();
  private final Map<String, Object> expectedCephStorage = new HashMap<>();

  @Before
  public void init() {
    cephService.clearStorage();
    expectedVariablesMap.clear();
    expectedCephStorage.clear();
    runtimeService.createProcessInstanceQuery().list().forEach(
        processInstance -> runtimeService
            .deleteProcessInstance(processInstance.getId(), "test clear"));
    testUserToken = TestUtils.getContent("/json/testuserAccessToken.json");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));
    CamundaAssertionUtil.setCephService(cephService);
  }

  @After
  public void destroy() {
    digitalSignatureMockServer.resetAll();
    dataFactoryMockServer.resetAll();
    userSettingsWireMock.resetAll();
    keycloakMockServer.resetAll();
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  protected void completeTask(String taskId, String processInstanceId, String formData) {
    var cephKey = cephKeyProvider.generateKey(taskId, processInstanceId);
    cephService.putFormData(cephKey, deserializeFormData(formData));
    String id = taskService.createTaskQuery().taskDefinitionKey(taskId).singleResult().getId();
    taskService.complete(id);
  }

  protected void completeTask(CompleteActivityDto completeActivityDto) {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    saveFormDataToCeph(completeActivityDto);

    var url = String.format("api/task/%s/complete", task(activityDefinitionId).getId());
    postForNoContent(url, "{}", completeActivityDto.getCompleterAccessToken());
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Map<String, List<Map<String, String>>>> completeTaskWithError(
      CompleteActivityDto completeActivityDto) throws JsonProcessingException {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    saveFormDataToCeph(completeActivityDto);

    var url = String.format("api/task/%s/complete", task(activityDefinitionId).getId());
    return (Map<String, Map<String, List<Map<String, String>>>>) postForObject(url, "{}", Map.class,
        completeActivityDto.getCompleterAccessToken());
  }

  private void saveFormDataToCeph(CompleteActivityDto completeActivityDto) {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    var processInstanceId = completeActivityDto.getProcessInstanceId();
    var cephKey = cephKeyProvider.generateKey(activityDefinitionId, processInstanceId);
    cephService.putContent(cephBucketName, cephKey,
        TestUtils.getContent(completeActivityDto.getExpectedFormData()));
  }

  protected void stubDataFactoryRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource());

    dataFactoryMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubDigitalSignatureRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath("/api/eseal/sign");

    digitalSignatureMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubSettingsRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER)
        .pathSegment(data.getResource());

    userSettingsWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubExcerptServiceRequest(StubData data) {
    var uriBuilder = Objects.nonNull(data.getUri()) ? data.getUri() :
        UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER).pathSegment(data.getResource());
    excerptServiceWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  private MappingBuilder getMappingBuilder(StubData data, UriComponentsBuilder uriBuilder) {
    if (Objects.nonNull(data.getResourceId())) {
      uriBuilder.pathSegment(data.getResourceId());
    }

    var mappingBuilder = getMappingBuilderForMethod(data.getHttpMethod(),
        uriBuilder.encode().toUriString());
    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    mappingBuilder.withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE));

    data.getQueryParams()
        .forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    if (Objects.nonNull(data.getRequestBody())) {
      mappingBuilder.withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())));
    }

    mappingBuilder
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(data.getResponse())));
    return mappingBuilder;
  }

  private MappingBuilder getMappingBuilderForMethod(HttpMethod method, String uri) {
    switch (method) {
      case GET:
        return get(urlPathEqualTo(uri));
      case POST:
        return post(urlPathEqualTo(uri));
      case PUT:
        return put(urlPathEqualTo(uri));
      case DELETE:
        return delete(urlPathEqualTo(uri));
      default:
        throw new NullPointerException("Stub method isn't defined");
    }
  }

  @SneakyThrows
  protected void stubSearchSubjects(String responseXmlFilePath) {
    stubTrembita(responseXmlFilePath, "SearchSubjects");
  }

  protected void stubSubjectDetail(String responseXmlFilePath) throws Exception {
    stubTrembita(responseXmlFilePath, "SubjectDetail");
  }

  private void stubTrembita(String responseXmlFilePath, String serviceCode) throws Exception {
    String response = Files.readString(
        Paths.get(TestUtils.class.getResource(responseXmlFilePath).toURI()),
        StandardCharsets.UTF_8);

    trembitaMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(String.format(".*%s.*", serviceCode)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }

  protected void assertCephContent() {
    expectedCephStorage.forEach((key, value) -> {
      var actualMap = cephService.getFormData(key);

      Assertions.assertThat(actualMap).get().isEqualTo(value);
    });
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(TestUtils.getContent(formData), FormDataDto.class);
    } catch (JsonProcessingException var4) {
      throw new IllegalStateException("Couldn't deserialize form data", var4);
    }
  }

  protected void addExpectedCephContent(String processInstanceId, String taskDefinitionKey,
      String cephContent) {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(cephContent));
  }

  protected void addExpectedVariable(String name, Object value) {
    expectedVariablesMap.put(name, value);
  }

  protected void addCompleterUsernameVariable(String taskDefinitionId, Object value) {
    expectedVariablesMap.put(String.format("%s_completer", taskDefinitionId), value);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceWithError(
      String processDefinitionKey, String token)
      throws JsonProcessingException {
    return (Map<String, Map<String, List<Map<String, String>>>>) postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        "{}", Map.class, token);
  }

  protected String startProcessInstance(String processDefinitionKey, String token)
      throws JsonProcessingException {
    var result = postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        "{}", Map.class, token);

    return (String) result.get("id");
  }

  protected String startProcessInstanceWithStartFormAndGetId(String processDefinitionKey,
      String startFormCephKey, String token) throws JsonProcessingException {
    var result = startProcessInstanceWithStartForm(processDefinitionKey, startFormCephKey, token);
    return (String) result.get("id");
  }

  protected Map startProcessInstanceWithStartForm(String processDefinitionKey,
      String startFormCephKey, String token) throws JsonProcessingException {
    var startProcessInstanceDto = new StartProcessInstanceDto();
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(startFormCephKey);
    startProcessInstanceDto.setVariables(
        Map.of(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME, variableValueDto));

     return postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        objectMapper.writeValueAsString(startProcessInstanceDto), Map.class, token);
  }

  protected void assertWaitingActivity(ProcessInstance processInstance,
      String searchLabFormActivityDefinitionKey, String formKey) {
    assertThat(processInstance).isWaitingAt(searchLabFormActivityDefinitionKey);
    assertThat(task(searchLabFormActivityDefinitionKey)).hasFormKey(formKey);
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  @SneakyThrows
  protected void assertSystemSignature(String processInstanceId, String variableName,
      String cephContent) {
    var variables = historyService().createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId).list();

    var signatureCephKeyVar = variables.stream()
        .filter(variable -> variable.getName().equals(variableName)).findAny();
    Assertions.assertThat(signatureCephKeyVar).isNotEmpty();

    var signatureCephKey = (String) signatureCephKeyVar.get().getValue();
    Assertions.assertThat(signatureCephKey)
        .matches("lowcode_" + processInstanceId + "_.+_system_signature_ceph_key");

    var cephDoc = cephService.getContent(cephBucketName, signatureCephKey);
    Assertions.assertThat(cephDoc).isPresent();

    var actual = objectMapper.readerForMapOf(Object.class).readValue(cephDoc.get());
    var expected = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(cephContent));

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  protected void executeWaitingJob(String activityDefinitionId) {
    var jobs = managementService().createJobQuery().activityId(activityDefinitionId).list();
    Assertions.assertThat(jobs).hasSize(1);
    jobs.forEach(job -> managementService().executeJob(job.getId()));
  }
}
