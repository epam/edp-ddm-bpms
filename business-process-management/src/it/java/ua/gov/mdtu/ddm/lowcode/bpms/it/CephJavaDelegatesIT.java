package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import ua.gov.mdtu.ddm.general.integration.ceph.exception.MisconfigurationException;

public class CephJavaDelegatesIT extends BaseIT {

  @Inject
  @Qualifier("cephWireMockServer")
  private WireMockServer cephWireMockServer;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Before
  public void init() {
    cephWireMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/" + cephBucketName + "/testKey"))
            .willReturn(aResponse()
                .withStatus(200))));
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/" + cephBucketName + "/testKey"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Length", "36")
                .withBody("{ \"var1\":\"value1\", \"var2\":\"value2\" }"))));
  }

  @After
  public void tearDown() {
    cephWireMockServer.resetRequests();
  }

  @Test
  @Deployment(resources = {"bpmn/testCephJavaDelegates.bpmn"})
  public void shouldThrowAnExceptionIfBucketNotExists() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<ListAllMyBucketsResult></ListAllMyBucketsResult>"))));

    Map<String, Object> vars = new HashMap<>();
    vars.put("key", "testKey");
    vars.put("content", "testContent");
    var ex = assertThrows(MisconfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars));

    assertThat(ex.getMessage(), is("Bucket bucket hasn't found"));
  }

  @Test
  @Deployment(resources = {"bpmn/testCephJavaDelegates.bpmn"})
  public void shouldUseCephJavaDelegatesInServiceTasks() {

    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));

    Map<String, Object> vars = new HashMap<>();
    vars.put("key", "testKey");
    vars.put("content", "testContent");
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars);

    assertTrue(process.isEnded());
    var variables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(process.getId()).list().stream()
        .filter(historicVariableInstance -> "content".equals(historicVariableInstance.getName()))
        .findFirst().orElseThrow();
    assertThat(variables.getValue(), is("{ \"var1\":\"value1\", \"var2\":\"value2\" }"));

    var rootUrlPattern = new UrlPattern(new EqualToPattern("/"), false);
    cephWireMockServer.verify(2, newRequestPattern(RequestMethod.GET, rootUrlPattern));

    UrlPattern lowcodeKeyUrlPattern = new UrlPattern(new EqualToPattern("/" + cephBucketName + "/testKey"), false);
    cephWireMockServer.verify(1, newRequestPattern(RequestMethod.GET, lowcodeKeyUrlPattern));
    cephWireMockServer.verify(1, newRequestPattern(RequestMethod.PUT, lowcodeKeyUrlPattern));
  }
}
