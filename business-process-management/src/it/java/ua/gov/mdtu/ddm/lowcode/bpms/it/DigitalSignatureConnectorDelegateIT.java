package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
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

public class DigitalSignatureConnectorDelegateIT extends BaseIT {

  private static final String CONTENT = "{\"x-access-token\":\"token\"}";

  @Inject
  @Qualifier("cephWireMockServer")
  private WireMockServer cephWireMockServer;
  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureConnectorDelegate.bpmn"})
  public void testDigitalSignatureConnectorDelegate() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/"))
            .willReturn(aResponse().withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));

    cephWireMockServer.addStubMapping(
        stubFor(get(urlMatching("/" + cephBucketName + "/cephKey"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Length", String.valueOf(CONTENT.length()))
                .withBody(CONTENT))));

    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\": \"data to sign\"}"))
            .willReturn(aResponse().withStatus(200).withBody("{\"signature\": \"test\"}"))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureConnectorDelegate_key", variables);
    assertThat(processInstance.isEnded()).isTrue();
  }
}
