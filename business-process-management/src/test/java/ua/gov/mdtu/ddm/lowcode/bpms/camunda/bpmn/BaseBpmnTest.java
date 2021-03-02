package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.VARIABLE_NAME;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.VARIABLE_VALUE;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.getContent;

import java.io.IOException;
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
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.DefineBusinessProcessStatusDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.GetFormDataFromCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.PutContentToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.PutFormDataToCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorCreateDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DataFactoryConnectorSearchDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector.DigitalSignatureConnectorDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;
import ua.gov.mdtu.ddm.lowcode.bpms.it.config.TestCephServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseBpmnTest {

  protected final String cephBucketName = "bucket";
  protected final String dataFactoryUrl = "http://data-factory:8080/";
  protected final String digitalSignatureUrl = "http://digital-signature-ops:8080/";

  protected final CephService cephService = new TestCephServiceImpl(cephBucketName);

  protected final RestTemplate restTemplate = new RestTemplate();

  protected MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  public void init() {

    GetFormDataFromCephDelegate getFormDataFromCephDelegate = new GetFormDataFromCephDelegate(
        cephBucketName, cephService);
    PutFormDataToCephDelegate putFormDataToCephDelegate = new PutFormDataToCephDelegate(
        cephBucketName, cephService);
    PutContentToCephDelegate putContentToCephDelegate = new PutContentToCephDelegate(cephBucketName,
        cephService);

    DataFactoryConnectorSearchDelegate dataFactoryConnectorSearchDelegate = new DataFactoryConnectorSearchDelegate(
        restTemplate, cephService, new JacksonJsonParser(), null, "bpms", cephBucketName,
        dataFactoryUrl);
    DataFactoryConnectorCreateDelegate dataFactoryConnectorCreateDelegate = new DataFactoryConnectorCreateDelegate(
        restTemplate, cephService, new JacksonJsonParser(), null, "bpms", cephBucketName,
        dataFactoryUrl);

    DigitalSignatureConnectorDelegate digitalSignatureConnectorDelegate = new DigitalSignatureConnectorDelegate(
        restTemplate, cephService, new JacksonJsonParser(), null, "bpms", cephBucketName,
        digitalSignatureUrl);

    Mocks.register("getFormDataFromCephDelegate", getFormDataFromCephDelegate);
    Mocks.register("putFormDataToCephDelegate", putFormDataToCephDelegate);
    Mocks.register("putContentToCephDelegate", putContentToCephDelegate);

    Mocks.register("dataFactoryConnectorSearchDelegate", dataFactoryConnectorSearchDelegate);
    Mocks.register("dataFactoryConnectorCreateDelegate", dataFactoryConnectorCreateDelegate);

    Mocks.register("digitalSignatureConnectorDelegate", digitalSignatureConnectorDelegate);

    Mocks.register("defineBusinessProcessStatusDelegate", new DefineBusinessProcessStatusDelegate());
  }

  protected void completeTask(String taskDefinitionKey, String formData,
      String processInstanceId) throws IOException {
    var variableName = String.format(VARIABLE_NAME, taskDefinitionKey);
    var variableValue = String.format(VARIABLE_VALUE, processInstanceId, variableName);
    cephService.putContent(cephBucketName, variableValue, getContent(formData));
    complete(task(taskDefinitionKey), withVariables(variableName, variableValue));
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
}
