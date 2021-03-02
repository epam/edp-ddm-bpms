package ua.gov.mdtu.ddm.lowcode.bpms.it.camunda.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.VARIABLE_NAME;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.VARIABLE_VALUE;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.getContent;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.io.IOException;
import javax.inject.Inject;
import org.assertj.core.util.Maps;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.it.BaseIT;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

public abstract class BaseBpmnIT extends BaseIT  {

  private final String MOCK_SERVER = "/mock-server";

  @Inject
  @Qualifier("digitalSignatureMockServer")
  protected WireMockServer digitalSignatureMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  protected WireMockServer dataFactoryMockServer;
  @Value("${ceph.bucket}")
  protected String cephBucketName;

  @Inject
  protected CephService cephService;

  protected void completeTask(String taskId, String processInstanceId, String formData)
      throws IOException {
    var variableName = String.format(VARIABLE_NAME, taskId);
    var variableValue = String.format(VARIABLE_VALUE, processInstanceId, variableName);
    cephService.putContent(cephBucketName, variableValue, getContent(formData));
    String id = taskService.createTaskQuery().taskDefinitionKey(taskId).singleResult().getId();
    taskService.complete(id, Maps.newHashMap(variableName, variableValue));
  }

  protected void stubDataFactorySearch(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource()).build().toUri();
    MappingBuilder mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200).withBody(getContent(data.getResponse())));

    data.getQueryParams().forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryCreate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource()).build().toUri();
    MappingBuilder mappingBuilder = post(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDigitalSignature(StubData data) throws IOException {
    MappingBuilder mappingBuilder = post(urlPathEqualTo("/api/eseal/sign"))
        .withRequestBody(equalToJson(getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));

    digitalSignatureMockServer.addStubMapping(stubFor(mappingBuilder));
  }
}
