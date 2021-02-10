package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;

public class CreateLaboratoryIT extends BaseIT {

  private static final String CONTENT = "{\"data\":{\"var1\":{\"value\":\"value1\"}}}";
  private static final String CONTENT_WITH_SIGNATURE =
      "{\"data\":{\"var1\":{\"value\":\"value1\"}}, \"signature\":\"signature1\"}";
  private static final String DATA_FACTORY_REQUEST = "{\"var1\":\"value1\"}";

  @Inject
  @Qualifier("cephWireMockServer")
  private WireMockServer cephWireMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  private WireMockServer dataFactoryMockServer;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Before
  public void setUp() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));

    cephWireMockServer.addStubMapping(
        stubFor(get(urlMatching("/" + cephBucketName
            + "/lowcode-.+-secure-sys-var-ref-task-form-data-Activity_1ne2ryq"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Length", String.valueOf(CONTENT.length()))
                .withBody(CONTENT))));

    cephWireMockServer.addStubMapping(
        stubFor(put(urlMatching("/" + cephBucketName
            + "/lowcode-.+-secure-sys-var-ref-task-form-data-Activity_0s05qmu"))
            .willReturn(aResponse().withStatus(200))));

    cephWireMockServer.addStubMapping(
        stubFor(get(urlMatching("/" + cephBucketName
            + "/lowcode-.+-secure-sys-var-ref-task-form-data-Activity_0s05qmu"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Length", String.valueOf(CONTENT_WITH_SIGNATURE.length()))
                .withBody(CONTENT_WITH_SIGNATURE))));
  }

  @Test
  @Deployment(resources = {"bpmn/testCreateLaboratory.bpmn"})
  public void testDataFactory500Error() {
    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/laboratory"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("source-system", equalTo("Low-code Platform"))
            .withHeader("source-application", equalTo("business-process-management"))
            .withRequestBody(equalTo(DATA_FACTORY_REQUEST))
            .willReturn(aResponse().withStatus(500))));

    var processInstance = runtimeService.startProcessInstanceByKey("add-lab");
    var processInstanceId = processInstance.getId();

    var createLabTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertThat(createLabTasks).hasSize(1);
    var createLabTaskId = createLabTasks.get(0).getId();
    Map<String, Object> createLabVariables = ImmutableMap.of(
        "secure-sys-var-ref-task-form-data-Activity_1ne2ryq",
        String.format("lowcode-%s-secure-sys-var-ref-task-form-data-Activity_1ne2ryq",
            processInstanceId));
    taskService.complete(createLabTaskId, createLabVariables);

    var signLabTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertThat(signLabTasks).hasSize(1);
    assertThat(signLabTasks.get(0)).isNotEqualTo(createLabTasks.get(0));
    var signLabTaskId = signLabTasks.get(0).getId();
    Map<String, Object> signLabVariables = ImmutableMap.of(
        "secure-sys-var-ref-task-form-data-Activity_0s05qmu",
        String.format("lowcode-%s-secure-sys-var-ref-task-form-data-Activity_0s05qmu",
            processInstanceId));
    assertThrows(CamundaSystemException.class,
        () -> taskService.complete(signLabTaskId, signLabVariables));
  }

  @Test
  @Deployment(resources = {"bpmn/testCreateLaboratory.bpmn"})
  public void testSuccessLabSaving() {
    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/laboratory"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("source-system", equalTo("Low-code Platform"))
            .withHeader("source-application", equalTo("business-process-management"))
            .withRequestBody(equalTo(DATA_FACTORY_REQUEST))
            .willReturn(aResponse().withStatus(201))));

    var processInstance = runtimeService.startProcessInstanceByKey("add-lab");
    var processInstanceId = processInstance.getId();

    var createLabTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertThat(createLabTasks).hasSize(1);
    var createLabTaskId = createLabTasks.get(0).getId();
    Map<String, Object> createLabVariables = ImmutableMap.of(
        "secure-sys-var-ref-task-form-data-Activity_1ne2ryq",
        String.format("lowcode-%s-secure-sys-var-ref-task-form-data-Activity_1ne2ryq",
            processInstanceId));
    taskService.complete(createLabTaskId, createLabVariables);

    var signLabTasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertThat(signLabTasks).hasSize(1);
    assertThat(signLabTasks.get(0)).isNotEqualTo(createLabTasks.get(0));
    var signLabTaskId = signLabTasks.get(0).getId();
    Map<String, Object> signLabVariables = ImmutableMap.of(
        "secure-sys-var-ref-task-form-data-Activity_0s05qmu",
        String.format("lowcode-%s-secure-sys-var-ref-task-form-data-Activity_0s05qmu",
            processInstanceId));
    taskService.complete(signLabTaskId, signLabVariables);

    var resultVariable = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .filter(historicVariableInstance -> "sys-var-process-completion-result"
            .equals(historicVariableInstance.getName()))
        .findFirst();
    assertThat(resultVariable).isNotEmpty();
    assertThat(resultVariable.get().getValue()).isEqualTo("Лабораторія створена");
  }
}
