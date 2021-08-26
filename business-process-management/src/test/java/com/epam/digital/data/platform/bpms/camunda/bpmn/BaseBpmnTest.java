package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.delegate.DefineBusinessProcessStatusDelegate;
import com.epam.digital.data.platform.bpms.delegate.DefineProcessExcerptIdDelegate;
import com.epam.digital.data.platform.bpms.delegate.UserDataValidationErrorDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.delegate.ceph.GetContentFromCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.GetFormDataFromCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.PutContentToCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.PutFormDataToCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorBatchReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorCreateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorSearchDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorUpdateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DigitalSignatureConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.ExcerptConnectorGenerateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.ExcerptConnectorStatusDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.UserSettingsConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.UserSettingsConnectorUpdateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.KeycloakAddRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.KeycloakGetUsersConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.KeycloakRemoveRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.registry.SearchSubjectsEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.bpms.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.bpms.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.spin.Spin;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  public static final String START_FORM_CEPH_KEY = "startFormCephKey";

  // init constants
  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String userSettingsBaseUrl = "http://user-setings:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";
  protected final String excerptServiceBaseUrl = "http://excerpt-service-api:8080/";
  protected final String springAppName = "business-process-management";

  // init mocks
  protected final MessageResolver messageResolver = mock(MessageResolver.class);
  protected final KeycloakRemoveRoleConnectorDelegate keycloakRemoveRoleConnectorDelegate =
      mock(KeycloakRemoveRoleConnectorDelegate.class);
  protected final KeycloakAddRoleConnectorDelegate keycloakAddRoleConnectorDelegate =
      mock(KeycloakAddRoleConnectorDelegate.class);
  protected final SearchSubjectsEdrRegistryConnectorDelegate searchSubjectsEdrRegistryConnectorDelegate = mock(
      SearchSubjectsEdrRegistryConnectorDelegate.class);
  protected final KeycloakGetUsersConnectorDelegate keycloakGetUsersConnectorDelegate = mock(
      KeycloakGetUsersConnectorDelegate.class);

  // init base classes for delegates
  protected ObjectMapper objectMapper;
  protected TestCephServiceImpl cephService;
  protected CephKeyProvider cephKeyProvider;
  protected RestTemplate restTemplate;

  protected MockRestServiceServer mockServer;

  protected String testUserName = "testuser";
  protected String testUserToken;

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();
  protected final Map<String, Object> expectedCephStorage = new HashMap<>();

  protected String currentProcessInstanceId;
  protected ProcessInstance currentProcessInstance;

  @Before
  public void init() {
    var beans = processEngineRule.getProcessEngineConfiguration().getBeans();

    objectMapper = (ObjectMapper) beans.get("objectMapper");
    cephService = (TestCephServiceImpl) beans.get("cephService");
    cephKeyProvider = (CephKeyProvider) beans.get("cephKeyProvider");

    var connectorResponseErrorHandler = new ConnectorResponseErrorHandler(objectMapper,
        messageResolver);
    restTemplate = new RestTemplateBuilder().errorHandler(connectorResponseErrorHandler).build();

    mockServer = MockRestServiceServer.createServer(restTemplate);

    initCephDelegates();
    initConnectorDelegates();

    var userDataValidationErrorDelegate = new UserDataValidationErrorDelegate(objectMapper);
    var defineBusinessProcessStatusDelegate = new DefineBusinessProcessStatusDelegate();
    var defineProcessExcerptIdDelegate = new DefineProcessExcerptIdDelegate();
    Mocks.register("defineBusinessProcessStatusDelegate", defineBusinessProcessStatusDelegate);
    Mocks.register("userDataValidationErrorDelegate", userDataValidationErrorDelegate);
    Mocks.register("defineProcessExcerptIdDelegate", defineProcessExcerptIdDelegate);

    // register keycloak delegates
    Mocks.register("keycloakRemoveRoleConnectorDelegate", keycloakRemoveRoleConnectorDelegate);
    Mocks.register("keycloakAddRoleConnectorDelegate", keycloakAddRoleConnectorDelegate);

    // register system beans
    Mocks.register("tokenParser", new TokenParser(objectMapper));
    Mocks.register("cephKeyProvider", cephKeyProvider);

    testUserToken = TestUtils.getContent("/json/testuserAccessToken.json");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));
    CamundaAssertionUtil.setCephService(cephService);
  }

  protected void completeTask(String taskDefinitionKey, String formData) {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, currentProcessInstanceId);
    cephService.putContent(cephBucketName, cephKey, TestUtils.getContent(formData));
    complete(task(taskDefinitionKey));
  }

  protected void completeTask(CompleteActivityDto completeActivityDto) {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    var cephKey = cephKeyProvider.generateKey(activityDefinitionId, currentProcessInstanceId);
    cephService.putContent(cephBucketName, cephKey,
        TestUtils.getContent(completeActivityDto.getExpectedFormData()));

    addExpectedCephContent(activityDefinitionId, completeActivityDto.getExpectedFormData());

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(completeActivityDto.getCompleterUserName(),
            completeActivityDto.getCompleterAccessToken()));
    complete(task(activityDefinitionId));
  }

  protected void mockDataFactoryRequest(StubData stubData) {
    mockRequest(stubData, dataFactoryUrl);
  }

  protected void mockExcerptRequest(StubData stubData) {
    mockRequest(stubData, excerptServiceBaseUrl);
  }

  protected void mockExcerptStatusRequest(StubData stubData) {
    mockRequest(stubData, excerptServiceBaseUrl, "status");
  }

  private void mockRequest(StubData stubData, String baseUrl, String... pathSegments) {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .pathSegment(stubData.getResource()).encode();
    if (Objects.nonNull(stubData.getResourceId())) {
      uriBuilder = uriBuilder.pathSegment(stubData.getResourceId());
    }
    if (Objects.nonNull(pathSegments)) {
      uriBuilder.pathSegment(pathSegments);
    }

    var responseActions = mockServer
        .expect(once(), requestTo(new UriMatcher(uriBuilder.build().toUri())))
        .andExpect(method(stubData.getHttpMethod()));

    stubData.getQueryParams()
        .forEach((name, value) -> responseActions.andExpect(queryParam(name, value)));
    stubData.getHeaders()
        .forEach((name, value) -> responseActions.andExpect(header(name, value)));

    if (Objects.nonNull(stubData.getRequestBody())) {
      responseActions.andExpect(content().json(TestUtils.getContent(stubData.getRequestBody())));
    }
    responseActions.andRespond(withStatus(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(TestUtils.getContent(stubData.getResponse())));
  }

  protected void mockDigitalSignatureSign(StubData stubData) {
    mockRequest(stubData, digitalSignatureUrl, "api", "eseal", "sign");
  }

  protected void mockSettingsRequest(StubData stubData) {
    mockRequest(stubData, userSettingsBaseUrl);
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
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Couldn't deserialize form data", ex);
    }
  }

  protected void addExpectedVariable(String name, Object value) {
    expectedVariablesMap.put(name, value);
  }

  protected void addExpectedCephContent(String taskDefinitionKey, String cephContent) {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, currentProcessInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(cephContent));
  }

  protected void assertWaitingActivity(String taskDefinitionKey, String formKey) {
    assertThat(currentProcessInstance).isWaitingAt(taskDefinitionKey);
    assertThat(task(taskDefinitionKey)).hasFormKey(formKey);
    assertThat(currentProcessInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }

  protected void startProcessInstance(String processDefinitionKey) {
    startProcessInstance(processDefinitionKey, null);
  }

  protected void startProcessInstance(String processDefinitionKey, Map<String, Object> vars) {
    currentProcessInstance = runtimeService().startProcessInstanceByKey(processDefinitionKey, vars);
    assertThat(currentProcessInstance).isStarted();
    currentProcessInstanceId = currentProcessInstance.getProcessInstanceId();
  }

  protected void executeWaitingJob(String activityDefinitionId) {
    var jobs = processEngineRule.getManagementService().createJobQuery()
        .activityId(activityDefinitionId).list();
    Assertions.assertThat(jobs).hasSize(1);
    jobs.forEach(job -> processEngineRule.getManagementService().executeJob(job.getId()));
  }

  private void initCephDelegates() {
    var getFormDataFromCephDelegate = new GetFormDataFromCephDelegate(cephService, cephKeyProvider);
    var putFormDataToCephDelegate = new PutFormDataToCephDelegate(cephService, objectMapper,
        cephKeyProvider);
    var putContentToCephDelegate = new PutContentToCephDelegate(cephBucketName, cephService);
    var getContentFromCephDelegate = new GetContentFromCephDelegate(cephBucketName, cephService);

    Mocks.register("getFormDataFromCephDelegate", getFormDataFromCephDelegate);
    Mocks.register("putFormDataToCephDelegate", putFormDataToCephDelegate);
    Mocks.register("putContentToCephDelegate", putContentToCephDelegate);
    Mocks.register("getContentFromCephDelegate", getContentFromCephDelegate);
  }

  private void initConnectorDelegates() {
    var digitalSignatureConnectorDelegate = new DigitalSignatureConnectorDelegate(restTemplate,
        springAppName, digitalSignatureUrl);
    Mocks.register("digitalSignatureConnectorDelegate", digitalSignatureConnectorDelegate);

    var dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, cephService, digitalSignatureConnectorDelegate, springAppName, cephBucketName,
        dataFactoryUrl);
    var dataFactoryConnectorBatchReadDelegate = new DataFactoryConnectorBatchReadDelegate(
        restTemplate, springAppName, dataFactoryUrl);
    var dataFactoryConnectorUpdateDelegate = new DataFactoryConnectorUpdateDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    Mocks.register("dataFactoryConnectorSearchDelegate", dataFactoryConnectorSearchDelegate);
    Mocks.register("dataFactoryConnectorCreateDelegate", dataFactoryConnectorCreateDelegate);
    Mocks.register("dataFactoryConnectorReadDelegate", dataFactoryConnectorReadDelegate);
    Mocks.register("dataFactoryConnectorBatchCreateDelegate",
        dataFactoryConnectorBatchCreateDelegate);
    Mocks.register("dataFactoryConnectorBatchReadDelegate", dataFactoryConnectorBatchReadDelegate);
    Mocks.register("dataFactoryConnectorUpdateDelegate", dataFactoryConnectorUpdateDelegate);

    var userSettingsConnectorReadDelegate = new UserSettingsConnectorReadDelegate(restTemplate,
        springAppName, userSettingsBaseUrl);
    var userSettingsConnectorUpdateDelegate = new UserSettingsConnectorUpdateDelegate(restTemplate,
        springAppName, userSettingsBaseUrl);
    Mocks.register("userSettingsConnectorReadDelegate", userSettingsConnectorReadDelegate);
    Mocks.register("userSettingsConnectorUpdateDelegate", userSettingsConnectorUpdateDelegate);

    Mocks.register("searchSubjectsEdrRegistryConnectorDelegate",
        searchSubjectsEdrRegistryConnectorDelegate);
    Mocks.register("keycloakGetUsersConnectorDelegate", keycloakGetUsersConnectorDelegate);

    var excerptConnectorGenerateDelegate = new ExcerptConnectorGenerateDelegate(restTemplate,
        springAppName, excerptServiceBaseUrl, objectMapper);
    Mocks.register("excerptConnectorGenerateDelegate", excerptConnectorGenerateDelegate);
    var excerptConnectorStatusDelegate = new ExcerptConnectorStatusDelegate(restTemplate,
        springAppName, excerptServiceBaseUrl, objectMapper);
    Mocks.register("excerptConnectorStatusDelegate", excerptConnectorStatusDelegate);
  }

  @SneakyThrows
  protected void mockEdrResponse(String responseBody) {
    reset(searchSubjectsEdrRegistryConnectorDelegate);
    doAnswer(invocation -> {
      var execution = (AbstractVariableScope) invocation.getArgument(0);
      execution.setVariableLocalTransient("response",
          EdrRegistryConnectorResponse.builder().responseBody(
              Spin.JSON(TestUtils.getContent(responseBody))).build());
      return null;
    }).when(searchSubjectsEdrRegistryConnectorDelegate).execute(any());
  }

  @SneakyThrows
  protected void mockGetKeycloakUsersConnectorDelegate(String officerUsers) {
    doAnswer(invocation -> {
      var execution = (AbstractVariableScope) invocation.getArgument(0);
      execution.setVariableLocal("usersByRole",
          objectMapper.readerForListOf(KeycloakUserDto.class)
              .readValue(TestUtils.getContent(officerUsers)));
      return null;
    }).when(keycloakGetUsersConnectorDelegate).execute(any());
  }

  @SneakyThrows
  protected void assertSystemSignature(String variableName, String cephContent) {
    var variables = historyService().createHistoricVariableInstanceQuery()
        .processInstanceId(currentProcessInstanceId).list();

    var signatureCephKeyVar = variables.stream()
        .filter(variable -> variable.getName().equals(variableName)).findAny();
    Assertions.assertThat(signatureCephKeyVar).isNotEmpty();

    var signatureCephKey = (String) signatureCephKeyVar.get().getValue();
    Assertions.assertThat(signatureCephKey)
        .matches("lowcode_" + currentProcessInstanceId + "_.+_system_signature_ceph_key");

    var cephDoc = cephService.getContent(cephBucketName, signatureCephKey);
    Assertions.assertThat(cephDoc).isPresent();

    var actual = objectMapper.readerForMapOf(Object.class).readValue(cephDoc.get());
    var expected = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(cephContent));

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  protected void startProcessInstanceWithStartForm(String processDefinitionId,
      FormDataDto formData) {
    cephService.putFormData(START_FORM_CEPH_KEY, formData);
    startProcessInstance(processDefinitionId,
        Map.of(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
            "initiator", testUserName));
  }

  protected void startProcessInstanceWithStartForm(String processDefinitionId,
      LinkedHashMap<String, Object> data) {
    startProcessInstanceWithStartForm(processDefinitionId,
        FormDataDto.builder().data(data).build());
  }

  @RequiredArgsConstructor
  private static class UriMatcher extends BaseMatcher<String> {

    private final URI expected;

    @Override
    public boolean matches(Object actual) {
      return expected.getRawPath().equals(URI.create((String) actual).getRawPath());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(expected.toString());
    }
  }
}
