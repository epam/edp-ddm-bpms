/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpm.it.bpmn;

import static com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil.processInstance;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import java.io.IOException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateStaffBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "update-personnel-bp";

  @Test
  @Deployment(resources = {"bpmn/update-personnel.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .response("/json/update-staff/data-factory/getStaffById.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("3758f3e6-937a-4ef9-a8b6-c95671241abd")
        .response("/json/update-staff/data-factory/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-status")
        .resourceId("cc974d44-362c-4caf-8a99-67780635ca68")
        .response("/json/update-staff/data-factory/getStaffStatusById.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/update-staff/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .requestBody("/json/update-staff/data-factory/updateStaffRequestBody.json")
        .response("/json/update-staff/data-factory/updateStaffRequestBody.json")
        .build());

    var data = deserializeFormData("/json/update-staff/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID,
        testUserToken, data);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-personnel-bp-update-personnel")
        .formKey("update-personnel-bp-update-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/update-staff/form-data/updatePersonnelPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-personnel-bp-update-personnel")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-staff/form-data/Activity_update-personnel-bp-update-personnel.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-personnel-bp-sign-personnel")
        .formKey("update-personnel-bp-sign-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/update-staff/form-data/Activity_update-personnel-bp-update-personnel.json"))
        .expectedVariables(
            Map.of("Activity_update-personnel-bp-update-personnel_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-personnel-bp-sign-personnel")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-staff/form-data/Activity_update-personnel-bp-sign-personnel.json")
        .build());

    addExpectedVariable("Activity_update-personnel-bp-sign-personnel_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад оновлені");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-staff/dso/digitalSignatureCephContent.json");
    assertThat(processInstance)
        .hasPassed(
            "Activity_update-personnel-bp-update-personnel",
            "Activity_update-personnel-bp-sign-personnel")
        .isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
