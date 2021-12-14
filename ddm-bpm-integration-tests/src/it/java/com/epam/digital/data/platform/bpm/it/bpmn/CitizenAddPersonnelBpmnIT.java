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

public class CitizenAddPersonnelBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "citizen-add-personnel";

  @Test
  @Deployment(resources = "bpmn/citizen-add-personnel.bpmn")
  public void happyPathTest() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c8";

    stubSearchSubjects("/xml/citizen-add-personnel/searchSubjectsActiveResponse.xml");
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/citizen-add-personnel/data-factory/searchLabResponse.json")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/citizen-add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var data = deserializeFormData("/json/citizen-add-personnel/form-data/start_event.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken,
        data);
    var processInstance = processInstance(processInstanceId);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signCitizenPersonnelFormActivity", processInstanceId)))
        .resource("staff")
        .requestBody("/json/citizen-add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addCitizenPersonnelFormActivity")
        .formKey("citizen-add-personnel-bp-add-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-personnel/form-data/addCitizenPersonnelFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addCitizenPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-add-personnel/form-data/addCitizenPersonnelFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signCitizenPersonnelFormActivity")
        .formKey("add-personnel-bp-sign-personnel")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-personnel/form-data/signCitizenPersonnelFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("addCitizenPersonnelFormActivity_completer", testUserName))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signCitizenPersonnelFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-add-personnel/form-data/signCitizenPersonnelFormActivity.json")
        .build());

    addExpectedVariable("signCitizenPersonnelFormActivity_completer", testUserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад внесені");

    assertSystemSignatureBathCreationForOneOperation(processInstanceId,
        "/json/citizen-add-personnel/dso/systemSignatureCephContent.json");
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
  }

  @Test
  @Deployment(resources = "bpmn/citizen-add-personnel.bpmn")
  public void validationErrorTest() throws JsonProcessingException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    stubSearchSubjects("/xml/citizen-add-personnel/searchSubjectsCancelledResponse.xml");

    var data = deserializeFormData("/json/citizen-add-personnel/form-data/start_event.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        data);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", ""),
        Map.entry("message", "Суб'єкт скасовано або припинено"),
        Map.entry("value", ""));
  }
}
