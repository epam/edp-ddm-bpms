/*
 * Copyright 2023 EPAM Systems.
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
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateDictBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "update-dict";

  @Test
  @Deployment(resources = {"bpmn/update-dict.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyPath() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("factor-equal-factor-type-name-count")
        .requestBody("{\"name\":\"testName\"}")
        .response("[]")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/update-dict/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_ID, testUserToken);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature", formDataKeyProvider
            .generateKey("Activity_update-dict-bp-sign-add-name", processInstanceId)))
        .resource("factor")
        .requestBody("/json/update-dict/data-factory/factorRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-add-name")
        .formKey("update-dict-bp-add-name")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-add-name")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-add-name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-sign-add-name")
        .formKey("update-dict-bp-sign-add-name")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-add-name.json"))
        .expectedVariables(Map.of("Activity_update-dict-bp-add-name_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("Activity_update-dict-bp-sign-add-name")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/update-dict/form-data/Activity_update-dict-bp-sign-add-name.json")
        .build());

    addExpectedVariable("Activity_update-dict-bp-sign-add-name_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Запис довідника створено");

    assertThat(processInstance)
        .hasPassed("Activity_update-dict-bp-add-name", "Activity_update-dict-bp-sign-add-name")
        .isEnded();
    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-dict/dso/digitalSignatureCephContent.json");
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
