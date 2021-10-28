package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddPersonnelBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_KEY = "add-personnel";

  @Test
  @Deployment(resources = {"bpmn/add-personnel.bpmn"})
  public void test() throws IOException {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("bb652d3f-a36f-465a-b7ba-232a5a1680c5")
        .response("/json/add-personnel/data-factory/findLaboratoryResponse.json")
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .requestBody("/json/add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    var startFormData = deserializeFormData("/json/add-personnel/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addPersonnelFormActivity")
        .formKey("add-personnel-bp-add-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-personnel/form-data/addPersonnelFormActivityPrepopulation.json"))
        .expectedVariables(
            Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-personnel/form-data/addPersonnelFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signPersonnelFormActivity")
        .formKey("add-personnel-bp-sign-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-personnel/form-data/signPersonnelFormActivityPrepopulation.json"))
        .expectedVariables(Map.of("addPersonnelFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-personnel/form-data/signPersonnelFormActivity.json")
        .build());

    var systemSignatureCephKey =
        "lowcode_" + currentProcessInstanceId + "_system_signature_ceph_key_0";

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("signPersonnelFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад внесені");

    assertSystemSignatureBathCreationForOneOperation(currentProcessInstanceId,
        "/json/add-personnel/dso/systemSignatureCephContent.json");
    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }
}
