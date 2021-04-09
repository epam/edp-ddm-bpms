package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.formDataVariableName;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.formDataVariableValue;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.getContent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.dto.FormDataDto;
import ua.gov.mdtu.ddm.general.localization.MessageResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.DefineBusinessProcessStatusDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.GetFormDataFromCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.PutContentToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.PutFormDataToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.UserDataValidationErrorDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DataFactoryConnectorBatchReadDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DataFactoryConnectorCreateDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DataFactoryConnectorReadDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DataFactoryConnectorSearchDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector.DigitalSignatureConnectorDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.handler.ConnectorResponseErrorHandler;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;
import ua.gov.mdtu.ddm.lowcode.bpms.it.config.TestCephServiceImpl;
import ua.gov.mdtu.ddm.lowcode.bpms.it.config.TestFormDataCephServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";
  protected final String springAppName = "business-process-management";

  protected final TestFormDataCephServiceImpl formDataCephService = new TestFormDataCephServiceImpl(
      cephBucketName);
  protected final TestCephServiceImpl cephService = new TestCephServiceImpl(cephBucketName);

  protected ObjectMapper objectMapper = new ObjectMapper();
  protected MessageResolver messageResolver = mock(MessageResolver.class);

  protected final RestTemplate restTemplate = new RestTemplateBuilder()
      .errorHandler(new ConnectorResponseErrorHandler(objectMapper, messageResolver)).build();

  protected MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();
  protected final Map<String, Object> expectedCephStorage = new HashMap<>();

  @Before
  public void init() {

    var getFormDataFromCephDelegate = new GetFormDataFromCephDelegate(objectMapper,
        formDataCephService);
    var putFormDataToCephDelegate = new PutFormDataToCephDelegate(objectMapper,
        formDataCephService);
    var putContentToCephDelegate = new PutContentToCephDelegate(cephBucketName, cephService);

    var dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(restTemplate,
        formDataCephService, springAppName, dataFactoryUrl);
    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        formDataCephService, springAppName, dataFactoryUrl);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        formDataCephService, springAppName, dataFactoryUrl);

    var digitalSignatureConnectorDelegate = new DigitalSignatureConnectorDelegate(restTemplate,
        formDataCephService, springAppName, digitalSignatureUrl);

    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, formDataCephService, cephService, digitalSignatureConnectorDelegate,
        springAppName, cephBucketName, dataFactoryUrl);

    var userDataValidationErrorDelegate = new UserDataValidationErrorDelegate(objectMapper);

    Mocks.register("getFormDataFromCephDelegate", getFormDataFromCephDelegate);
    Mocks.register("putFormDataToCephDelegate", putFormDataToCephDelegate);
    Mocks.register("putContentToCephDelegate", putContentToCephDelegate);

    Mocks.register("dataFactoryConnectorSearchDelegate", dataFactoryConnectorSearchDelegate);
    Mocks.register("dataFactoryConnectorCreateDelegate", dataFactoryConnectorCreateDelegate);
    Mocks.register("dataFactoryConnectorReadDelegate", dataFactoryConnectorReadDelegate);
    Mocks.register("dataFactoryConnectorBatchCreateDelegate",
        dataFactoryConnectorBatchCreateDelegate);

    Mocks.register("digitalSignatureConnectorDelegate", digitalSignatureConnectorDelegate);

    Mocks
        .register("defineBusinessProcessStatusDelegate", new DefineBusinessProcessStatusDelegate());

    Mocks.register("userDataValidationErrorDelegate", userDataValidationErrorDelegate);

    Mocks.register("dataFactoryConnectorBatchReadDelegate",
        new DataFactoryConnectorBatchReadDelegate(restTemplate, formDataCephService, springAppName,
            dataFactoryUrl));
  }

  protected void completeTask(String taskDefinitionKey, String formData,
      String processInstanceId) throws IOException {
    var variableName = formDataVariableName(taskDefinitionKey);
    var variableValue = formDataVariableValue(processInstanceId, variableName);
    formDataCephService.putFormData(variableValue, deserializeFormData(getContent(formData)));
    complete(task(taskDefinitionKey), withVariables(variableName, variableValue));
  }

  protected void mockDataFactoryGet(StubData stubData) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode();
    stubData.getQueryParams().forEach(uriBuilder::queryParam);
    mockServer.expect(ExpectedCount.once(), requestTo(uriBuilder.toUriString()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(getContent(stubData.getResponse())));
  }

  protected void mockDataFactorySearch(StubData stubData) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource()).encode();
    stubData.getQueryParams().forEach(uriBuilder::queryParam);
    mockServer.expect(ExpectedCount.once(), requestTo(uriBuilder.toUriString()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(getContent(stubData.getResponse())));
  }

  protected void mockDataFactoryCreate(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource()).build().toUri();
    mockServer.expect(ExpectedCount.once(), requestTo(uri.toString()))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json(getContent(stubData.getRequestBody())))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(getContent(stubData.getResponse()))
        );
  }

  protected void mockDigitalSignatureSign(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromHttpUrl(digitalSignatureUrl)
        .pathSegment("api", "eseal", "sign").build().toUri();
    mockServer.expect(ExpectedCount.once(), requestTo(uri.toString()))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json(getContent(stubData.getRequestBody())))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(getContent(stubData.getResponse()))
        );
  }

  protected void assertCephContent() {
    expectedCephStorage.forEach((key, value) -> {
      var actualMap = formDataCephService.getFormData(key);

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

  protected void addExpectedCephContent(String processInstanceId, String taskDefinitionKey,
      String cephContent) throws IOException {
    var refVarName = formDataVariableName(taskDefinitionKey);
    var cephKey = formDataVariableValue(processInstanceId, refVarName);

    expectedVariablesMap.put(refVarName, cephKey);
    expectedCephStorage.put(cephKey, deserializeFormData(getContent(cephContent)));
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
