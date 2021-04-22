package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.epam.digital.data.platform.bpms.delegate.DefineBusinessProcessStatusDelegate;
import com.epam.digital.data.platform.bpms.delegate.UserDataValidationErrorDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.delegate.ceph.GetFormDataFromCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.PutContentToCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.ceph.PutFormDataToCephDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorBatchReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorCreateDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorReadDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DataFactoryConnectorSearchDelegate;
import com.epam.digital.data.platform.bpms.delegate.connector.DigitalSignatureConnectorDelegate;
import com.epam.digital.data.platform.bpms.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.config.TestFormDataCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
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

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";
  protected final String springAppName = "business-process-management";

  protected final TestFormDataCephServiceImpl formDataCephService = new TestFormDataCephServiceImpl(
      cephBucketName);
  protected final TestCephServiceImpl cephService = new TestCephServiceImpl(cephBucketName);
  protected final CephKeyProvider cephKeyProvider = new CephKeyProvider();

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

    var getFormDataFromCephDelegate = new GetFormDataFromCephDelegate(formDataCephService, cephKeyProvider);
    var putFormDataToCephDelegate = new PutFormDataToCephDelegate(formDataCephService,
        objectMapper, cephKeyProvider);
    var putContentToCephDelegate = new PutContentToCephDelegate(cephBucketName, cephService);

    var dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(restTemplate,
        formDataCephService, cephKeyProvider, springAppName, dataFactoryUrl);
    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        formDataCephService, cephKeyProvider, springAppName, dataFactoryUrl);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        formDataCephService, cephKeyProvider, springAppName, dataFactoryUrl);

    var digitalSignatureConnectorDelegate = new DigitalSignatureConnectorDelegate(restTemplate,
        formDataCephService, cephKeyProvider, springAppName, digitalSignatureUrl);

    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, formDataCephService, cephService, digitalSignatureConnectorDelegate, cephKeyProvider,
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
        new DataFactoryConnectorBatchReadDelegate(restTemplate, formDataCephService, cephKeyProvider,
            springAppName, dataFactoryUrl));
  }

  protected void completeTask(String taskDefinitionKey, String formData,
      String processInstanceId) throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    formDataCephService.putFormData(cephKey, deserializeFormData(TestUtils.getContent(formData)));
    complete(task(taskDefinitionKey));
  }

  protected void mockDataFactoryGet(StubData stubData) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode();
    stubData.getQueryParams().forEach(uriBuilder::queryParam);
    mockServer.expect(ExpectedCount.once(), requestTo(uriBuilder.toUriString()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestUtils.getContent(stubData.getResponse())));
  }

  protected void mockDataFactorySearch(StubData stubData) throws IOException {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource()).encode();
    stubData.getQueryParams().forEach(uriBuilder::queryParam);
    mockServer.expect(ExpectedCount.once(), requestTo(uriBuilder.toUriString()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestUtils.getContent(stubData.getResponse())));
  }

  protected void mockDataFactoryCreate(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryUrl)
        .pathSegment(stubData.getResource()).build().toUri();
    mockServer.expect(ExpectedCount.once(), requestTo(uri.toString()))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json(TestUtils.getContent(stubData.getRequestBody())))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestUtils.getContent(stubData.getResponse()))
        );
  }

  protected void mockDigitalSignatureSign(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromHttpUrl(digitalSignatureUrl)
        .pathSegment("api", "eseal", "sign").build().toUri();
    mockServer.expect(ExpectedCount.once(), requestTo(uri.toString()))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json(TestUtils.getContent(stubData.getRequestBody())))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestUtils.getContent(stubData.getResponse()))
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
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(TestUtils.getContent(cephContent)));
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
