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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.it.BaseIT;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
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
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseBpmnIT extends BaseIT {

  private final String MOCK_SERVER = "/mock-server";
  private final String SETTINGS_MOCK_SERVER = "/user-settings-mock-server";

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
  @Qualifier("trembitaMockServer")
  protected WireMockServer trembitaMockServer;
  @Value("${ceph.bucket}")
  protected String cephBucketName;

  @Inject
  protected ObjectMapper objectMapper;
  @Inject
  protected CephKeyProvider cephKeyProvider;

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
  }

  @After
  public void destroy() {
    digitalSignatureMockServer.resetAll();
    dataFactoryMockServer.resetAll();
    userSettingsWireMock.resetAll();
    keycloakMockServer.resetAll();
  }

  protected void completeTask(String taskId, String processInstanceId, String formData)
      throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskId, processInstanceId);
    cephService.putFormData(cephKey, deserializeFormData(TestUtils.getContent(formData)));
    String id = taskService.createTaskQuery().taskDefinitionKey(taskId).singleResult().getId();
    taskService.complete(id);
  }

  protected void stubDataFactoryRequest(StubData data) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource());

    dataFactoryMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubDigitalSignatureRequest(StubData data) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromPath("/api/eseal/sign");

    digitalSignatureMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubSettingsRequest(StubData data) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER)
        .pathSegment(data.getResource());

    userSettingsWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  private MappingBuilder getMappingBuilder(StubData data, UriComponentsBuilder uriBuilder)
      throws IOException {
    if (Objects.nonNull(data.getResourceId())) {
      uriBuilder.pathSegment(data.getResourceId());
    }

    var mappingBuilder = getMappingBuilderForMethod(data.getHttpMethod(), uriBuilder.toUriString());
    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));

    data.getQueryParams()
        .forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    if (Objects.nonNull(data.getRequestBody())) {
      mappingBuilder.withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())));
    }

    mappingBuilder
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));
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

  protected void stubDataFactoryCreate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource()).build()
        .toUri();
    MappingBuilder mappingBuilder = post(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryRead(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryUpdate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromUri(URI.create(MOCK_SERVER)).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = put(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryGet(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode().build().toUri();
    var mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(stubData.getResponse())));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubUserSettingsRead(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode().build().toUri();
    var mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(stubData.getResponse())));
    userSettingsWireMock.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubUserSettingsUpdate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromUri(URI.create(SETTINGS_MOCK_SERVER)).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = put(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    userSettingsWireMock.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDigitalSignature(StubData data) throws IOException {
    MappingBuilder mappingBuilder = post(urlPathEqualTo("/api/eseal/sign"))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));

    digitalSignatureMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubSearchSubjects(String responseXmlFilePath) throws Exception {
    stubTrembita(responseXmlFilePath, "SearchSubjects");
  }

  protected void stubSubjectDetail(String responseXmlFilePath) throws Exception {
    stubTrembita(responseXmlFilePath, "SubjectDetail");
  }

  private void stubTrembita(String responseXmlFilePath, String serviceCode) throws Exception {
    String response = Files.readString(
        Paths.get(TestUtils.class.getResource(responseXmlFilePath).toURI()), StandardCharsets.UTF_8);

    trembitaMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(String.format(".*%s.*", serviceCode)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }

  protected void assertCephContent() {
    expectedCephStorage.forEach((key, value) -> {
      var actualMap = cephService.getFormData(key).get();

      Assertions.assertThat(actualMap).isEqualTo(value);
    });
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(formData, FormDataDto.class);
    } catch (JsonProcessingException var4) {
      var4.clearLocation();
      throw new IllegalStateException("Couldn't deserialize form data", var4);
    }
  }

  protected void addExpectedCephContent(String processInstanceId, String taskDefinitionKey,
      String cephContent) throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(TestUtils.getContent(cephContent)));
  }

  protected void addExpectedVariable(String name, Object value) {
    expectedVariablesMap.put(name, value);
  }

  protected void addCompleterUsernameVariable(String taskDefinitionId, Object value){
    expectedVariablesMap.put(String.format("%s_completer", taskDefinitionId), value);
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
}
