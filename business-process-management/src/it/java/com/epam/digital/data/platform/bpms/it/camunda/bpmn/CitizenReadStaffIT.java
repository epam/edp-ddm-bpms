package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenReadStaffIT extends BaseBpmnIT {

  @Test
  public void test() throws JsonProcessingException {
    var staffId = "02e68684-1335-47f0-9bd6-17d937267527";
    var labId = "3758f3e6-937a-4ef9-a8b6-c95671241abd";
    var researchId = "1238f3e6-937a-4ef9-a8b6-c95671241123";

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
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("research")
        .resourceId(researchId)
        .response("/json/read-staff/researchByIdResponse.json")
        .build());

    var data = new LinkedHashMap<String, Object>();
    data.put("staff", Map.of("staffId", staffId));
    data.put("laboratory", Map.of("laboratoryId", labId));
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-read-personnel-data",
        testUserToken, FormDataDto.builder().data(data).build());
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    BpmnAwareTests.assertThat(processInstance)
        .isWaitingAt("Activity_read-personnel-bp-read-personnel");
    completeTask("Activity_read-personnel-bp-read-personnel", processInstanceId,
        "/json/citizen-read-staff/Activity_read-personnel-bp-read-personnel.json");

    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("Activity_read-personnel-bp-read-personnel")
        .isEnded();

    assertThat(processInstance).variables().containsKey("sys-var-process-completion-result");
    assertThat(processInstance).variables().containsValue("Дані про кадровий склад відображені");
  }
}
