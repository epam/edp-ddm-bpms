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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class NestedEntityBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "nested_entity_test_process";

  @Test
  @Deployment(resources = "bpmn/nested_entity_test_process.bpmn")
  public void test() throws JsonProcessingException {
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody(
            "/json/nested_entity_test_process/createPersonProfileSystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("person-profile")
        .requestBody("/json/nested_entity_test_process/createPersonProfileRequest.json")
        .response("{\"personProfileId\":\"personProfileId\"}")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody(
            "/json/nested_entity_test_process/createNestedTransactionEntitySystemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("nested/nested-transaction-entity")
        .requestBody("/json/nested_entity_test_process/createNestedTransactionEntityRequest.json")
        .response("{\"personProfileId\":\"personProfileId\"}")
        .build());

    var processInstanceId = startProcessInstance(PROCESS_DEFINITION_KEY, testUserToken);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addPersonProfileActivity")
        .formKey("add-dataprof")
        .assignee(testUserName)
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addPersonProfileActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/nested_entity_test_process/addPersonProfileActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signPersonProfileActivity")
        .formKey("add-signd")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(
            deserializeFormData("/json/nested_entity_test_process/addPersonProfileActivity.json"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signPersonProfileActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/nested_entity_test_process/signPersonProfileActivity.json")
        .build());

    var processInstances = historyService().createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).orderByProcessInstanceEndTime().asc()
        .list();
    Assertions.assertThat(processInstances).hasSize(2);

    var personProfileSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(0).getId() + "_system_signature_ceph_key";
    addExpectedVariable("person_profile_system_signature_ceph_key",
        personProfileSystemSignatureCephKey);

    assertSystemSignature(processInstanceId, "person_profile_system_signature_ceph_key",
        "/json/nested_entity_test_process/createPersonProfileSystemSignatureCephData.json");

    var transactionSystemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        processInstances.get(1).getId() + "_system_signature_ceph_key";
    addExpectedVariable("transaction_system_signature_ceph_key", transactionSystemSignatureCephKey);

    assertSystemSignature(processInstanceId, "transaction_system_signature_ceph_key",
        "/json/nested_entity_test_process/createNestedTransactionEntitySystemCephData.json");

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }
}
