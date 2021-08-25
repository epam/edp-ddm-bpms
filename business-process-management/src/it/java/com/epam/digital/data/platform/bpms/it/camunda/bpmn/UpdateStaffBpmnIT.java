package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateStaffBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/update-personnel.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {
    var staffId = "02e68684-1335-47f0-9bd6-17d937267527";
    var labId = "3758f3e6-937a-4ef9-a8b6-c95671241abd";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId(staffId)
        .response("/json/update-staff/getStaffById.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/update-staff/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-status")
        .resourceId("cc974d44-362c-4caf-8a99-67780635ca68")
        .response("/json/update-staff/getStaffStatusById.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/update-staff/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .requestBody("/json/update-staff/updateStaffRequestBody.json")
        .response("/json/update-staff/updateStaffRequestBody.json")
        .build());

    //start process
    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("staff", Map.of("staffId", staffId));
    data.put("laboratory", Map.of("laboratoryId", labId));
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("update-personnel-bp",
        "startFormCephKey", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    BpmnAwareTests.assertThat(processInstance)
        .isWaitingAt("Activity_update-personnel-bp-update-personnel");
    completeTask("Activity_update-personnel-bp-update-personnel", processInstanceId,
        "/json/update-staff/Activity_update-personnel-bp-update-personnel.json");

    BpmnAwareTests.assertThat(processInstance)
        .isWaitingAt("Activity_update-personnel-bp-sign-personnel");
    completeTask("Activity_update-personnel-bp-sign-personnel", processInstanceId,
        "/json/update-staff/Activity_update-personnel-bp-sign-personnel.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed(
            "Activity_update-personnel-bp-update-personnel",
            "Activity_update-personnel-bp-sign-personnel")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-staff/digitalSignatureCephContent.json");
  }
}
