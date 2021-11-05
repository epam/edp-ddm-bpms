package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenReadStaffIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "citizen-read-personnel-data";

  @Test
  @Deployment(resources = "bpmn/citizen-read-personnel-data.bpmn")
  public void test() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .response("/json/citizen-read-staff/data-factory/getStaffById.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("3758f3e6-937a-4ef9-a8b6-c95671241abd")
        .response("/json/citizen-read-staff/data-factory/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-status")
        .resourceId("cc974d44-362c-4caf-8a99-67780635ca68")
        .response("/json/citizen-read-staff/data-factory/getStaffStatusById.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("research")
        .resourceId("1238f3e6-937a-4ef9-a8b6-c95671241123")
        .response("/json/citizen-read-staff/data-factory/researchByIdResponse.json")
        .build());

    var data = deserializeFormData("/json/citizen-read-staff/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-read-personnel-data",
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_read-personnel-bp-read-personnel")
        .formKey("read-personnel-data-bp-read-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-read-staff/form-data/PrePopulation_Activity_read-personnel-bp-read-personnel.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_read-personnel-bp-read-personnel")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-read-staff/form-data/Activity_read-personnel-bp-read-personnel.json")
        .build());

    addExpectedVariable("Activity_read-personnel-bp-read-personnel_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад відображені");

    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("Activity_read-personnel-bp-read-personnel").isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
