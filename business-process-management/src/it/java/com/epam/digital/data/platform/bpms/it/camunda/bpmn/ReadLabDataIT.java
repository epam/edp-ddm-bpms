package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class ReadLabDataIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "read-lab";

  @Test
  @Deployment(resources = "bpmn/read-lab-data.bpmn")
  public void happyPath() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/read-lab/data-factory/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322996")
        .response("/json/read-lab/data-factory/koatuuByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "92cb1462-ec57-4b87-9e8d-594e0c322996"))
        .response("/json/read-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322997")
        .response("/json/read-lab/data-factory/koatuuOblByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("19aab23b-1e49-4064-8f7e-39735ece4388")
        .response("/json/read-lab/data-factory/findOwnershipResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("a790eb71-6015-4f40-995b-ad474e8eddca")
        .response("/json/read-lab/data-factory/findKopfgResponse.json")
        .build());

    var data = deserializeFormData("/json/read-lab/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataFormActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/read-lab/form-data/prePopulationViewLabDataFormActivity.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("viewLabDataFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/read-lab/form-data/prePopulationViewLabDataFormActivity.json")
        .build());

    addExpectedVariable("viewLabDataFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію відображені");

    BpmnAwareTests.assertThat(processInstance).hasPassed("viewLabDataFormActivity").isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
