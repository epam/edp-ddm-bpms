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
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenUpdatePersonnelBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "citizen-update-personnel";

  @Test
  @Deployment(resources = {"bpmn/citizen-update-personnel.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testUpdateCitizenPersonnel() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-personnel/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .response("/json/citizen-update-personnel/data-factory/searchStaffResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        .response("/json/citizen-update-personnel/data-factory/searchLabResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("staff-status")
        .resourceId("cc974d44-362c-4caf-8a99-67780635ca68")
        .response("/json/citizen-update-personnel/data-factory/readStaffStatusResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("research")
        .resourceId("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        .response("/json/citizen-update-personnel/data-factory/readResearchResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-update-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData(
        "/json/citizen-update-personnel/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            formDataKeyProvider
                .generateKey("signUpdatedCitizenPersonnelFormActivity", processInstanceId)))
        .resource("staff")
        .resourceId("02e68684-1335-47f0-9bd6-17d937267527")
        .requestBody("/json/citizen-update-personnel/data-factory/updateStaffRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateCitizenPersonnelFormActivity")
        .formKey("citizen-update-personnel-bp-update-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-personnel/form-data/citizenPersonnelFormActivity.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("updateCitizenPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-personnel/form-data/updatedCitizenPersonnelFormForSignActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signUpdatedCitizenPersonnelFormActivity")
        .formKey("update-personnel-bp-sign-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-personnel/form-data/updatedCitizenPersonnelFormForSignActivity.json"))
        .expectedVariables(Map.of("updateCitizenPersonnelFormActivity_completer", testUserName))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signUpdatedCitizenPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-personnel/form-data/updatedCitizenPersonnelFormForSignActivity.json")
        .build());

    addExpectedVariable("signUpdatedCitizenPersonnelFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад оновлені");

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/citizen-update-personnel/dso/systemSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-personnel.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testValidationError() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-update-personnel/searchSubjectsCancelledResponse.xml");

    var startFormData = deserializeFormData(
        "/json/citizen-update-personnel/form-data/startFormDataActivity.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_ID, testUserToken,
        startFormData);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", ""),
        Map.entry("message", "Суб'єкт скасовано або припинено"),
        Map.entry("value", ""));
  }
}
