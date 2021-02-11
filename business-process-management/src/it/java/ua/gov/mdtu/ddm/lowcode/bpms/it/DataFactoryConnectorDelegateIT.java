package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public class DataFactoryConnectorDelegateIT extends BaseIT {

  private static final String CONTENT = "{\"x-access-token\":\"token\"}";

  @Inject
  @Qualifier("cephWireMockServer")
  private WireMockServer cephWireMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  private WireMockServer dataFactoryMockServer;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key");
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorDeleteDelegate.bpmn"})
  public void testDataFactoryConnectorDeleteDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(400))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorDeleteDelegate_key");
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorUpdateDelegate.bpmn"})
  public void testDataFactoryConnectorUpdateDelegate() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));

    cephWireMockServer.addStubMapping(
        stubFor(get(urlMatching("/" + cephBucketName + "/cephKey"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Length", String.valueOf(CONTENT.length()))
                .withBody(CONTENT))));

    dataFactoryMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withHeader("X-Digital-Signature", equalTo("cephKey"))
            .withHeader("X-Digital-Signature-Derived", equalTo("cephKey"))
            .withRequestBody(equalTo("{\"var\", \"value\"}"))
            .willReturn(aResponse().withStatus(500))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");
    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorUpdateDelegate_key", variables);
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorSearchDelegate.bpmn"})
  public void testDataFactoryConnectorSearchDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory"))
            .withQueryParam("id", equalTo("id1"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .withHeader("x-custom-header", equalTo("custom header value"))
            .willReturn(aResponse().withStatus(200))));

    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlEqualTo("/mock-server/laboratory"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(400))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorSearchDelegate_key");
    assertThat(processInstance.isEnded()).isTrue();
  }
}
