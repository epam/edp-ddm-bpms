/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.extension.delegate.DefineBusinessProcessStatusDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.DefineProcessExcerptIdDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.UserDataValidationErrorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.extension.delegate.ceph.GetContentFromCephDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.ceph.GetFormDataFromCephDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.ceph.PutContentToCephDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.ceph.PutFormDataToCephDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.BaseConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchReadDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorCreateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorSearchDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorUpdateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.DigitalSignatureConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.ExcerptConnectorGenerateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.ExcerptConnectorStatusDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.UserSettingsConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.UserSettingsConnectorUpdateDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen.KeycloakAddCitizenRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.citizen.KeycloakRemoveCitizenRoleConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak.officer.KeycloakGetOfficerUsersConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr.SearchSubjectsEdrRegistryConnectorDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.KeycloakUserDto;
import com.epam.digital.data.platform.bpms.extension.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.bpms.listener.PutFormDataToCephTaskListener;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.starter.security.jwt.TokenParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
import org.springframework.test.util.ReflectionTestUtils;
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
  protected final String springAppName = "ddm-bpm";

  // init mocks
  protected final MessageResolver messageResolver = mock(MessageResolver.class);
  protected final KeycloakRemoveCitizenRoleConnectorDelegate keycloakRemoveRoleConnectorDelegate =
      mock(KeycloakRemoveCitizenRoleConnectorDelegate.class);
  protected final KeycloakAddCitizenRoleConnectorDelegate keycloakAddRoleConnectorDelegate =
      mock(KeycloakAddCitizenRoleConnectorDelegate.class);
  protected final SearchSubjectsEdrRegistryConnectorDelegate searchSubjectsEdrRegistryConnectorDelegate = mock(
      SearchSubjectsEdrRegistryConnectorDelegate.class);
  protected final KeycloakGetOfficerUsersConnectorDelegate keycloakGetUsersConnectorDelegate = mock(
      KeycloakGetOfficerUsersConnectorDelegate.class);

  // init base classes for delegates
  protected ObjectMapper objectMapper;
  protected TestCephServiceImpl cephService;
  protected CephKeyProvider cephKeyProvider;
  protected RestTemplate restTemplate;
  protected NamedVariableAccessorFactory namedVarAccessorFactory;
  protected VariableAccessorFactory varAccessorFactory;

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
    varAccessorFactory = (VariableAccessorFactory) beans.get("variableAccessorFactory");
    namedVarAccessorFactory = new BaseNamedVariableAccessorFactory(varAccessorFactory);
    initPutFormDataToCephTaskListener(beans);

    var connectorResponseErrorHandler = new ConnectorResponseErrorHandler(objectMapper,
        messageResolver);
    restTemplate = new RestTemplateBuilder().errorHandler(connectorResponseErrorHandler).build();

    mockServer = MockRestServiceServer.createServer(restTemplate);

    initCephDelegates();
    initConnectorDelegates();

    initUserDataValidationDelegate();
    initDefineBusinessProcessStatusDelegate();
    initDefineProcessExcerptIdDelegate();

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

  private void initPutFormDataToCephTaskListener(Map<Object, Object> beans) {
    var listener = (PutFormDataToCephTaskListener) beans.get("putFormDataToCephListener");
    var formDataVar = namedVarAccessorFactory.variableAccessor("userTaskInputFormDataPrepopulate",
        true);
    ReflectionTestUtils.setField(listener, "userTaskInputFormDataPrepopulateVariable", formDataVar);
  }

  private void initDefineBusinessProcessStatusDelegate() {
    var delegate = new DefineBusinessProcessStatusDelegate(
        new ProcessCompletionResultVariable(varAccessorFactory));
    var statusVar = namedVarAccessorFactory.variableAccessor("status", false);

    ReflectionTestUtils.setField(delegate, "statusVariable", statusVar);
    Mocks.register("defineBusinessProcessStatusDelegate", delegate);
  }

  private void initUserDataValidationDelegate() {
    var delegate = new UserDataValidationErrorDelegate(objectMapper);
    var validationErrorsVar = namedVarAccessorFactory.variableAccessor("validationErrors",
        false);

    ReflectionTestUtils.setField(delegate, "validationErrorsVariable", validationErrorsVar);
    Mocks.register("userDataValidationErrorDelegate", delegate);
  }

  private void initDefineProcessExcerptIdDelegate() {
    var delegate = new DefineProcessExcerptIdDelegate(
        new ProcessExcerptIdVariable(varAccessorFactory));
    var excerptIdVariable = namedVarAccessorFactory.variableAccessor("excerptId", false);

    ReflectionTestUtils.setField(delegate, "excerptIdVariable", excerptIdVariable);
    Mocks.register("defineProcessExcerptIdDelegate", delegate);
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
    initGetFormDataFromCephDelegate();
    initPutFormDataToCephDelegate();
    initPutContentToCephDelegate();
    initGetContentFromCephDelegate();
  }

  private void initGetFormDataFromCephDelegate() {
    var delegate = new GetFormDataFromCephDelegate(cephService, cephKeyProvider);
    var taskDefinitionKeyVar = namedVarAccessorFactory.variableAccessor("taskDefinitionKey",
        false);
    var formDataVariable = namedVarAccessorFactory.variableAccessor("formData", true);

    ReflectionTestUtils.setField(delegate, "taskDefinitionKeyVariable", taskDefinitionKeyVar);
    ReflectionTestUtils.setField(delegate, "formDataVariable", formDataVariable);
    Mocks.register("getFormDataFromCephDelegate", delegate);
  }

  private void initPutFormDataToCephDelegate() {
    var delegate = new PutFormDataToCephDelegate(cephService, cephKeyProvider, objectMapper);
    var taskDefinitionKeyVar = namedVarAccessorFactory.variableAccessor("taskDefinitionKey",
        false);
    var formDataVariable = namedVarAccessorFactory.variableAccessor("formData", true);

    ReflectionTestUtils.setField(delegate, "taskDefinitionKeyVariable", taskDefinitionKeyVar);
    ReflectionTestUtils.setField(delegate, "formDataVariable", formDataVariable);
    Mocks.register("putFormDataToCephDelegate", delegate);
  }

  private void initPutContentToCephDelegate() {
    var delegate = new PutContentToCephDelegate(cephBucketName, cephService);
    var keyVariable = namedVarAccessorFactory.variableAccessor("key", false);
    var contentVariable = namedVarAccessorFactory.variableAccessor("content", true);

    ReflectionTestUtils.setField(delegate, "keyVariable", keyVariable);
    ReflectionTestUtils.setField(delegate, "contentVariable", contentVariable);
    Mocks.register("putContentToCephDelegate", delegate);
  }

  private void initGetContentFromCephDelegate() {
    var delegate = new GetContentFromCephDelegate(cephBucketName, cephService);
    var keyVariable = namedVarAccessorFactory.variableAccessor("key", false);
    var contentVariable = namedVarAccessorFactory.variableAccessor("content", true);

    ReflectionTestUtils.setField(delegate, "keyVariable", keyVariable);
    ReflectionTestUtils.setField(delegate, "contentVariable", contentVariable);
    Mocks.register("getContentFromCephDelegate", delegate);
  }

  private void initConnectorDelegates() {
    DigitalSignatureConnectorDelegate delegate = initDigitalSignatureConnectorDelegate();

    initDataFactoryConnectorSearchDelegate();

    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    initConnectorVariables(dataFactoryConnectorCreateDelegate);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    initConnectorVariables(dataFactoryConnectorReadDelegate);
    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, cephService, delegate, springAppName, cephBucketName,
        dataFactoryUrl);
    initConnectorVariables(dataFactoryConnectorBatchCreateDelegate);

    initDataFactoryBatchReadDelegate();

    var dataFactoryConnectorUpdateDelegate = new DataFactoryConnectorUpdateDelegate(restTemplate,
        springAppName, dataFactoryUrl);
    initConnectorVariables(dataFactoryConnectorUpdateDelegate);
    Mocks.register("dataFactoryConnectorCreateDelegate", dataFactoryConnectorCreateDelegate);
    Mocks.register("dataFactoryConnectorReadDelegate", dataFactoryConnectorReadDelegate);
    Mocks.register("dataFactoryConnectorBatchCreateDelegate",
        dataFactoryConnectorBatchCreateDelegate);
    Mocks.register("dataFactoryConnectorUpdateDelegate", dataFactoryConnectorUpdateDelegate);

    var userSettingsConnectorReadDelegate = new UserSettingsConnectorReadDelegate(restTemplate,
        springAppName, userSettingsBaseUrl);
    initConnectorVariables(userSettingsConnectorReadDelegate);
    var userSettingsConnectorUpdateDelegate = new UserSettingsConnectorUpdateDelegate(restTemplate,
        springAppName, userSettingsBaseUrl);
    initConnectorVariables(userSettingsConnectorUpdateDelegate);
    Mocks.register("userSettingsConnectorReadDelegate", userSettingsConnectorReadDelegate);
    Mocks.register("userSettingsConnectorUpdateDelegate", userSettingsConnectorUpdateDelegate);

    Mocks.register("searchSubjectsEdrRegistryConnectorDelegate",
        searchSubjectsEdrRegistryConnectorDelegate);
    Mocks.register("keycloakGetUsersConnectorDelegate", keycloakGetUsersConnectorDelegate);

    initExcerptConnectorGenerateDelegate();
    initExcerptConnectorStatusDelegate();
  }

  private DigitalSignatureConnectorDelegate initDigitalSignatureConnectorDelegate() {
    var delegate = new DigitalSignatureConnectorDelegate(restTemplate, springAppName,
        digitalSignatureUrl);
    initConnectorVariables(delegate);
    var responseVariable = namedVarAccessorFactory.variableAccessor("response", true);

    ReflectionTestUtils.setField(delegate, "dsoResponseVariable", responseVariable);
    Mocks.register("digitalSignatureConnectorDelegate", delegate);
    return delegate;
  }

  private void initConnectorVariables(BaseConnectorDelegate delegate) {
    var xAccessTokenVariable = namedVarAccessorFactory.variableAccessor("x_access_token",
        false);
    var xDigitalSignatureCephKeyVariable = namedVarAccessorFactory.variableAccessor(
        "x_digital_signature_ceph_key", false);
    var xDigitalSignatureDerivedCephKeyVariable = namedVarAccessorFactory.variableAccessor(
        "x_digital_signature_derived_ceph_key", false);
    var headersVariable = namedVarAccessorFactory.variableAccessor("headers", false);

    ReflectionTestUtils.setField(delegate, "xAccessTokenVariable", xAccessTokenVariable);
    ReflectionTestUtils.setField(delegate, "xDigitalSignatureCephKeyVariable",
        xDigitalSignatureCephKeyVariable);
    ReflectionTestUtils.setField(delegate, "xDigitalSignatureDerivedCephKeyVariable",
        xDigitalSignatureDerivedCephKeyVariable);
    ReflectionTestUtils.setField(delegate, "headersVariable", headersVariable);

    var resourceVariable = namedVarAccessorFactory.variableAccessor("resource", false);
    var resourceIdVariable = namedVarAccessorFactory.variableAccessor("id", false);
    var payloadVariable = namedVarAccessorFactory.variableAccessor("payload", true);
    var responseVariable = namedVarAccessorFactory.variableAccessor("response", true);

    ReflectionTestUtils.setField(delegate, "resourceVariable", resourceVariable);
    ReflectionTestUtils.setField(delegate, "resourceIdVariable", resourceIdVariable);
    ReflectionTestUtils.setField(delegate, "payloadVariable", payloadVariable);
    ReflectionTestUtils.setField(delegate, "responseVariable", responseVariable);
  }

  private void initDataFactoryConnectorSearchDelegate() {
    var delegate = new DataFactoryConnectorSearchDelegate(restTemplate, springAppName,
        dataFactoryUrl);
    initConnectorVariables(delegate);
    var searchConditionsVariable = namedVarAccessorFactory.variableAccessor("searchConditions",
        false);
    ReflectionTestUtils.setField(delegate, "searchConditionsVariable", searchConditionsVariable);
    Mocks.register("dataFactoryConnectorSearchDelegate", delegate);
  }

  private void initDataFactoryBatchReadDelegate() {
    var delegate = new DataFactoryConnectorBatchReadDelegate(restTemplate, springAppName,
        dataFactoryUrl);
    initConnectorVariables(delegate);
    var resourceIdsVariable = namedVarAccessorFactory.variableAccessor("resourceIds", false);
    ReflectionTestUtils.setField(delegate, "resourceIdsVariable", resourceIdsVariable);
    Mocks.register("dataFactoryConnectorBatchReadDelegate", delegate);
  }

  private void initExcerptConnectorStatusDelegate() {
    var delegate = new ExcerptConnectorStatusDelegate(restTemplate, springAppName,
        excerptServiceBaseUrl);
    initConnectorVariables(delegate);
    var excerptIdentifierVariable = namedVarAccessorFactory.variableAccessor(
        "excerptIdentifier", false);
    ReflectionTestUtils.setField(delegate, "excerptIdentifierVariable", excerptIdentifierVariable);
    Mocks.register("excerptConnectorStatusDelegate", delegate);
  }

  private void initExcerptConnectorGenerateDelegate() {
    var delegate = new ExcerptConnectorGenerateDelegate(restTemplate, springAppName,
        excerptServiceBaseUrl, objectMapper);

    initConnectorVariables(delegate);
    var excerptTypeVariable = namedVarAccessorFactory.variableAccessor("excerptType", false);
    var excerptInputDataVariable = namedVarAccessorFactory.variableAccessor("excerptInputData",
        false);
    var requiresSystemSignatureVariable = namedVarAccessorFactory.variableAccessor(
        "requiresSystemSignature", false);
    ReflectionTestUtils.setField(delegate, "excerptTypeVariable", excerptTypeVariable);
    ReflectionTestUtils.setField(delegate, "requiresSystemSignatureVariable",
        requiresSystemSignatureVariable);
    ReflectionTestUtils.setField(delegate, "excerptInputDataVariable", excerptInputDataVariable);

    Mocks.register("excerptConnectorGenerateDelegate", delegate);
  }

  @SneakyThrows
  protected void mockEdrResponse(String responseBody) {
    reset(searchSubjectsEdrRegistryConnectorDelegate);
    doAnswer(invocation -> {
      var execution = (AbstractVariableScope) invocation.getArgument(0);
      execution.setVariableLocalTransient("response",
          RegistryConnectorResponse.builder().responseBody(
              Spin.JSON(TestUtils.getContent(responseBody))).build());
      return null;
    }).when(searchSubjectsEdrRegistryConnectorDelegate).execute(any(DelegateExecution.class));
  }

  @SneakyThrows
  protected void mockGetKeycloakUsersConnectorDelegate(String officerUsers) {
    doAnswer(invocation -> {
      var execution = (AbstractVariableScope) invocation.getArgument(0);
      execution.setVariableLocal("usersByRole",
          objectMapper.readerForListOf(KeycloakUserDto.class)
              .readValue(TestUtils.getContent(officerUsers)));
      return null;
    }).when(keycloakGetUsersConnectorDelegate).execute(any(DelegateExecution.class));
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

    assertSignature(signatureCephKey, cephContent);
  }

  protected void assertSystemSignatureBathCreationForOneOperation(String processInstanceId,
      String cephContent) {
    var systemSignatureCephKey = String
        .format("lowcode_%s_system_signature_ceph_key_0", processInstanceId);
    assertSignature(systemSignatureCephKey, cephContent);
  }

  @SneakyThrows
  private void assertSignature(String systemSignatureCephKey, String cephContent) {
    var signature = cephService.getContent(cephBucketName, systemSignatureCephKey);
    Assertions.assertThat(signature).isNotEmpty();

    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(signature.get());
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(cephContent));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);
  }

  protected void startProcessInstanceWithStartForm(String processDefinitionId,
      FormDataDto formData) {
    cephService.putFormData(START_FORM_CEPH_KEY, formData);
    startProcessInstance(processDefinitionId, Map.of(
        StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, START_FORM_CEPH_KEY,
        "initiator", testUserName));
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
