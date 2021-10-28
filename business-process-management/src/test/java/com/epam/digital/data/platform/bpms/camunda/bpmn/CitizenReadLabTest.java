package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenReadLabTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = "bpmn/citizen-read-lab.bpmn")
  public void happyPathTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToRead")
        .response("/json/citizen-read-lab/data-factory/laboratoryToRead.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToReadKoatuu")
        .response("/json/citizen-read-lab/data-factory/labToReadKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "labToReadKoatuu"))
        .response("/json/citizen-read-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToReadKoatuu")
        .response("/json/citizen-read-lab/data-factory/labToReadKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToReadOwnership")
        .response("/json/citizen-read-lab/data-factory/labToReadOwnership.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToReadKopfg")
        .response("/json/citizen-read-lab/data-factory/labToReadKopfg.json")
        .build());

    var startFormData = deserializeFormData("/json/citizen-read-lab/form-data/start_event.json");
    startProcessInstanceWithStartForm("citizen-read-lab", startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-read-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-read-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-read-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    addExpectedVariable("viewLabDataCitizenActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію відображені");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }
}
