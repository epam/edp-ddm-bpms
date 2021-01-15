package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;
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

    assertThat(ex.getMessage()).isEqualTo("Bucket bucket hasn't found");
  }

  @Test
  @Deployment(resources = {"bpmn/testCephJavaDelegates.bpmn"})
  public void shouldUseCephJavaDelegatesInServiceTasks() {
    initGetCephBucket();

    Map<String, Object> vars = ImmutableMap.of(
        "key", "testKey",
        "content", "testContent");
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars);

    assertTrue(process.isEnded());
    var variables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(process.getId()).list().stream()
        .filter(historicVariableInstance -> "content".equals(historicVariableInstance.getName()))
        .findFirst().orElseThrow();
    assertThat(variables.getValue()).isEqualTo("{ \"var1\":\"value1\", \"var2\":\"value2\" }");

    var rootUrlPattern = new UrlPattern(new EqualToPattern("/"), false);
    cephWireMockServer.verify(2, newRequestPattern(RequestMethod.GET, rootUrlPattern));

    UrlPattern lowcodeKeyUrlPattern = new UrlPattern(
        new EqualToPattern("/" + cephBucketName + "/testKey"), false);
    cephWireMockServer.verify(1, newRequestPattern(RequestMethod.GET, lowcodeKeyUrlPattern));
    cephWireMockServer.verify(1, newRequestPattern(RequestMethod.PUT, lowcodeKeyUrlPattern));
  }

  @Test
  public void shouldPutTaskFormDataToCeph() {
    var content = "{\"name\":{\"value\":\"value ek\"}}";

    initGetCephBucket();
    cephWireMockServer.addStubMapping(
        stubFor(put(urlMatching("/" + cephBucketName
            + "/lowcode-.+-secure-sys-var-ref-task-form-data-userTask"))
            .willReturn(aResponse().withStatus(200))));
    cephWireMockServer.addStubMapping(
        stubFor(get(urlMatching("/" + cephBucketName
            + "/lowcode-.+-secure-sys-var-ref-task-form-data-userTask"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Length", "29").withBody(content))));

    Map<String, Object> vars = ImmutableMap.of("formData", Variables.stringValue(content, true));
    var processInstance = runtimeService
        .startProcessInstanceByKey("testCephFormDataDelegates_key", "key", vars);

    assertTrue(processInstance.isEnded());

    var resultVariables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .collect(toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
            (o1, o2) -> o1));

    var expectedCephKey = "lowcode-" + processInstance.getProcessInstanceId()
        + "-secure-sys-var-ref-task-form-data-userTask";
    assertThat(resultVariables)
        .containsEntry("secure-sys-var-ref-task-form-data-userTask", expectedCephKey)
        .containsEntry("formDataOutput", content);

    UrlPattern lowcodeKeyUrlPattern = new UrlPattern(
        new EqualToPattern("/" + cephBucketName + "/" + expectedCephKey), false);
    cephWireMockServer.verify(1, newRequestPattern(RequestMethod.PUT, lowcodeKeyUrlPattern));
  }

  private void initGetCephBucket() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));
  }
}
