package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
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
    cephService.setCephBucketName(cephBucketName);
  }

  @Test
  @Deployment(resources = {"bpmn/testCephJavaDelegates.bpmn"})
  public void shouldThrowAnExceptionIfBucketNotExists() {
    String contentToPut = "{ \"var1\":\"value1\", \"var2\":\"value2\" }";
    Map<String, Object> vars = new HashMap<>();
    vars.put("key", "testKey");
    vars.put("content", Variables.stringValue(contentToPut, true));

    this.cephService.setCephBucketName("newName");
    var ex = assertThrows(MisconfigurationException.class, () -> runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars));

    assertThat(ex.getMessage()).isEqualTo("Bucket bucket hasn't found");
  }

  @Test
  @Deployment(resources = {"bpmn/testCephJavaDelegates.bpmn"})
  public void shouldUseCephJavaDelegatesInServiceTasks() {
    String contentToPut = "{ \"var1\":\"value1\", \"var2\":\"value2\" }";

    Map<String, Object> vars = ImmutableMap.of(
        "key", "testKey",
        "content", Variables.stringValue(contentToPut, true));
    ProcessInstance process = runtimeService
        .startProcessInstanceByKey("testCephJavaDelegates_key", "1", vars);

    assertTrue(process.isEnded());

    String content = cephService.getContent(cephBucketName, "testKey");
    assertThat(content).isNotNull();
    assertThat(content).isEqualTo(contentToPut);
  }

  @Test
  public void shouldPutTaskFormDataToCeph() {
    var content = "{\"name\":{\"value\":\"value ek\"}}";

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
        .doesNotContainKey("formDataOutput");

    String data = cephService.getContent(cephBucketName, expectedCephKey);
    assertThat(data).isNotNull();
    assertThat(data).isEqualTo(content);
  }

  @Test
  public void shouldSaveProcessStatusAsSysVariable() {
    var status = "awesome";
    Map<String, Object> vars = ImmutableMap.of("status", status);
    var processInstance = runtimeService
        .startProcessInstanceByKey("testDefineProcessStatus_key", "key", vars);

    assertTrue(processInstance.isEnded());

    var resultVariables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .collect(toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
            (o1, o2) -> o1));
    assertThat(resultVariables).containsEntry("sys-var-process-completion-result", status);
  }
}
