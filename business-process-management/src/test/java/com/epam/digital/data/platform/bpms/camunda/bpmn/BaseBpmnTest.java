package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.epam.digital.data.platform.bpms.delegate.DefineBusinessProcessStatusDelegate;
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
import com.epam.digital.data.platform.bpms.delegate.connector.DigitalSignatureConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.UserSettingsConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.UserSettingsConnectorUpdateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.KeycloakAddRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.keycloak.KeycloakRemoveRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  // init constants
  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String userSettingsBaseUrl = "http://user-setings:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";
  protected final String springAppName = "business-process-management";

  // init mocks
  protected final MessageResolver messageResolver = mock(MessageResolver.class);
  protected final KeycloakRemoveRoleConnectorDelegate keycloakRemoveRoleConnectorDelegate =
      mock(KeycloakRemoveRoleConnectorDelegate.class);
  protected final KeycloakAddRoleConnectorDelegate keycloakAddRoleConnectorDelegate =
      mock(KeycloakAddRoleConnectorDelegate.class);

  // init base classes for delegates
  protected final ObjectMapper objectMapper = new ObjectMapper();
  protected final TestCephServiceImpl cephService =
      new TestCephServiceImpl(cephBucketName, objectMapper);
  protected final CephKeyProvider cephKeyProvider = new CephKeyProvider();
  protected final ConnectorResponseErrorHandler connectorResponseErrorHandler =
      new ConnectorResponseErrorHandler(objectMapper, messageResolver);
  protected final RestTemplate restTemplate =
      new RestTemplateBuilder().errorHandler(connectorResponseErrorHandler).build();

  protected MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();
  protected final Map<String, Object> expectedCephStorage = new HashMap<>();

  protected String currentProcessInstanceId;
  protected ProcessInstance currentProcessInstance;

  @Before
  public void init() {
    initCephDelegates();
    initConnectorDelegates();

    var userDataValidationErrorDelegate = new UserDataValidationErrorDelegate(objectMapper);
    var defineBusinessProcessStatusDelegate = new DefineBusinessProcessStatusDelegate();
    Mocks.register("defineBusinessProcessStatusDelegate", defineBusinessProcessStatusDelegate);
    Mocks.register("userDataValidationErrorDelegate", userDataValidationErrorDelegate);

    // register keycloak delegates
    Mocks.register("keycloakRemoveRoleConnectorDelegate", keycloakRemoveRoleConnectorDelegate);
    Mocks.register("keycloakAddRoleConnectorDelegate", keycloakAddRoleConnectorDelegate);

    // register system beans
    Mocks.register("tokenParser", new TokenParser(objectMapper));
    Mocks.register("cephKeyProvider", cephKeyProvider);
  }

  protected void completeTask(String taskDefinitionKey, String formData) throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, currentProcessInstanceId);
    cephService.putContent(cephBucketName, cephKey, TestUtils.getContent(formData));
    complete(task(taskDefinitionKey));
  }

  protected void mockDataFactoryRequest(StubData stubData) throws IOException {
    mockRequest(stubData, dataFactoryUrl);
  }

  private void mockRequest(StubData stubData, String baseUrl, String... pathSegments)
      throws IOException {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .pathSegment(stubData.getResource()).encode();
    if (Objects.nonNull(pathSegments)) {
      uriBuilder.pathSegment(pathSegments);
    }
    if (Objects.nonNull(stubData.getResourceId())) {
      uriBuilder = uriBuilder.pathSegment(stubData.getResourceId());
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

  protected void mockDigitalSignatureSign(StubData stubData) throws IOException {
    mockRequest(stubData, digitalSignatureUrl, "api", "eseal", "sign");
  }

  protected void mockSettingsRequest(StubData stubData) throws IOException {
    mockRequest(stubData, userSettingsBaseUrl);
  }

  protected void assertCephContent() {
    expectedCephStorage.forEach((key, value) -> {
      var actualMap = cephService.getFormData(key);

      Assertions.assertThat(actualMap).isEqualTo(value);
    });
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(formData, FormDataDto.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Couldn't deserialize form data", ex);
    }
  }

  protected void addExpectedVariable(String name, Object value) {
    expectedVariablesMap.put(name, value);
  }

  protected void addExpectedCephContent(String taskDefinitionKey, String cephContent)
      throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, currentProcessInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(TestUtils.getContent(cephContent)));
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
        cephService, cephKeyProvider, springAppName, digitalSignatureUrl);
    Mocks.register("digitalSignatureConnectorDelegate", digitalSignatureConnectorDelegate);

    var dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(restTemplate,
        cephService, cephKeyProvider, springAppName, dataFactoryUrl);
    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        cephService, cephKeyProvider, springAppName, dataFactoryUrl);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        cephService, cephKeyProvider, springAppName, dataFactoryUrl);
    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, cephService, cephService, digitalSignatureConnectorDelegate, cephKeyProvider,
        springAppName, cephBucketName, dataFactoryUrl);
    var dataFactoryConnectorBatchReadDelegate = new DataFactoryConnectorBatchReadDelegate(
        restTemplate, cephService, cephKeyProvider, springAppName, dataFactoryUrl);
    Mocks.register("dataFactoryConnectorSearchDelegate", dataFactoryConnectorSearchDelegate);
    Mocks.register("dataFactoryConnectorCreateDelegate", dataFactoryConnectorCreateDelegate);
    Mocks.register("dataFactoryConnectorReadDelegate", dataFactoryConnectorReadDelegate);
    Mocks.register("dataFactoryConnectorBatchCreateDelegate",
        dataFactoryConnectorBatchCreateDelegate);
    Mocks.register("dataFactoryConnectorBatchReadDelegate", dataFactoryConnectorBatchReadDelegate);

    var userSettingsConnectorReadDelegate = new UserSettingsConnectorReadDelegate(restTemplate,
        cephKeyProvider, cephService, springAppName, userSettingsBaseUrl);
    var userSettingsConnectorUpdateDelegate = new UserSettingsConnectorUpdateDelegate(restTemplate,
        cephKeyProvider, cephService, springAppName, userSettingsBaseUrl);
    Mocks.register("userSettingsConnectorReadDelegate", userSettingsConnectorReadDelegate);
    Mocks.register("userSettingsConnectorUpdateDelegate", userSettingsConnectorUpdateDelegate);
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
