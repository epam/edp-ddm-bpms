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

public class AddLabBpmnTest extends BaseBpmnTest {

  private static final String PROCESS_DEFINITION_KEY = "add-lab";

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "77777777", "name", "labName"))
        .build());
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-lab/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData("/json/add-lab/form-data/start_event.json");
    startProcessInstanceWithStartForm(PROCESS_DEFINITION_KEY, startFormData);

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signLabFormActivity", currentProcessInstanceId)))
        .resource("laboratory")
        .requestBody("/json/add-lab/data-factory/addLabRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .formKey("add-lab-bp-add-lab")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-lab/form-data/addLabFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-lab/form-data/addLabFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabFormActivity")
        .formKey("shared-sign-lab")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/add-lab/form-data/signLabFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/add-lab/form-data/signLabFormActivity.json")
        .build());

    addExpectedVariable("signLabFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторія створена");

    assertThat(currentProcessInstance).hasPassed("addLabFormActivity", "signLabFormActivity")
        .isEnded();
    assertSystemSignature("system_signature_ceph_key",
        "/json/add-lab/dso/digitalSignatureCephContent.json");
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }
}
