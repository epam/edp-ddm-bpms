package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn;

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

import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.DefineBusinessProcessStatusDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.GetFormDataFromCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.PutContentToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.PutFormDataToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorBatchCreateDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorCreateDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorReadDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorSearchDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DigitalSignatureConnectorDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.service.MessageResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;
import ua.gov.mdtu.ddm.lowcode.bpms.it.config.TestCephServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";
  protected final String springAppName = "business-process-management";

  protected final TestCephServiceImpl cephService = new TestCephServiceImpl(cephBucketName);

  protected final RestTemplate restTemplate = new RestTemplate();

  protected MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

  protected JacksonJsonParser jacksonJsonParser = new JacksonJsonParser();
  protected MessageResolver messageResolver = mock(MessageResolver.class);

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  public void init() {

    var getFormDataFromCephDelegate = new GetFormDataFromCephDelegate(cephBucketName, cephService);
    var putFormDataToCephDelegate = new PutFormDataToCephDelegate(cephBucketName, cephService);
    var putContentToCephDelegate = new PutContentToCephDelegate(cephBucketName, cephService);

    var dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(restTemplate,
        cephService, jacksonJsonParser, messageResolver, springAppName, cephBucketName,
        dataFactoryUrl);
    var dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(restTemplate,
        cephService, jacksonJsonParser, messageResolver, springAppName, cephBucketName,
        dataFactoryUrl);
    var dataFactoryConnectorReadDelegate = new DataFactoryConnectorReadDelegate(restTemplate,
        cephService, jacksonJsonParser, messageResolver, springAppName, cephBucketName,
        dataFactoryUrl);
    var dataFactoryConnectorBatchCreateDelegate = new DataFactoryConnectorBatchCreateDelegate(
        restTemplate, cephService, jacksonJsonParser, messageResolver, springAppName,
        cephBucketName, dataFactoryUrl);

    var digitalSignatureConnectorDelegate = new DigitalSignatureConnectorDelegate(restTemplate,
        cephService, jacksonJsonParser, messageResolver, springAppName, cephBucketName,
        digitalSignatureUrl);

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
  }

  protected void completeTask(String taskDefinitionKey, String formData,
      String processInstanceId) throws IOException {
    var variableName = formDataVariableName(taskDefinitionKey);
    var variableValue = formDataVariableValue(processInstanceId, variableName);
    cephService.putContent(cephBucketName, variableValue, getContent(formData));
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

  protected void assertCephContent(Map<String, String> expectedContent) {
    Assertions.assertThat(cephService.getStorage()).hasSize(expectedContent.size());
    expectedContent.forEach((key, value) -> {
      var expectedMap = jacksonJsonParser.parseMap(value);
      var actualMap = jacksonJsonParser.parseMap(cephService.getContent(cephBucketName, key));

      Assertions.assertThat(actualMap).isEqualTo(expectedMap);
    });
  }
}
