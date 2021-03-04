package ua.gov.mdtu.ddm.lowcode.bpms.it.camunda.bpmn;

import java.io.IOException;
import java.util.HashMap;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

public class UpdateStaffBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/update-personnel-bp.bpmn"})
  public void test() throws IOException {

    stubDataFactoryRead(StubData.builder()
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .response("/json/update-staff/getStaffById.json")
        .build());

    stubDataFactoryRead(StubData.builder()
        .resource("laboratory")
        .resourceId("3758f3e6-937a-4ef9-a8b6-c95671241abd")
        .response("/json/update-staff/laboratoryByIdResponse.json")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/update-staff/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryUpdate(StubData.builder()
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .requestBody("/json/update-staff/updateStaffRequestBody.json")
        .response("/json/update-staff/updateStaffRequestBody.json")
        .build());

    //start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("update-personnel-bp", new HashMap<>());
    String processInstanceId = processInstance.getId();


    BpmnAwareTests.assertThat(processInstance).isWaitingAt("Activity_update-personnel-bp-search-personnel");
    completeTask("Activity_update-personnel-bp-search-personnel", processInstanceId, "/json/update-staff/Activity_update-personnel-bp-search-personnel.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("Activity_update-personnel-bp-update-personnel");
    completeTask("Activity_update-personnel-bp-update-personnel", processInstanceId, "/json/update-staff/Activity_update-personnel-bp-update-personnel.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("Activity_update-personnel-bp-sign-personnel");
    completeTask("Activity_update-personnel-bp-sign-personnel", processInstanceId, "/json/update-staff/Activity_update-personnel-bp-sign-personnel.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed(
            "Activity_update-personnel-bp-search-personnel",
            "Activity_update-personnel-bp-update-personnel",
            "Activity_update-personnel-bp-sign-personnel")
        .isEnded();
  }
}
