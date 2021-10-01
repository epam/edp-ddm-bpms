package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenReadLabBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = "bpmn/citizen-read-lab.bpmn")
  public void happyPathTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToRead")
        .response("/json/citizen-read-lab/data-factory/laboratoryToRead.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToReadKoatuu")
        .response("/json/citizen-read-lab/data-factory/labToReadKoatuu.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "labToReadKoatuu"))
        .response("/json/citizen-read-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToReadOwnership")
        .response("/json/citizen-read-lab/data-factory/labToReadOwnership.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToReadKopfg")
        .response("/json/citizen-read-lab/data-factory/labToReadKopfg.json")
        .build());

    var startFormData = deserializeFormData("/json/citizen-read-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId("citizen-read-lab",
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-read-lab")
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-read-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-read-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    addExpectedVariable("viewLabDataCitizenActivity_completer", testUserName);
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію відображені");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
