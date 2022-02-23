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

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import java.io.IOException;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CreateAppExcludeBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_KEY = "create-app-exclude";

  @Test
  @Deployment(resources = {"bpmn/create-app-exclude.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testHappyPath() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/data-factory/lastLaboratorySolutionResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/applicationTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/solutionTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(laboratoryId)
        .response("/json/create-app-exclude/data-factory/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/create-app-exclude/data-factory/solutionTypeEqualConstantCodeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/create-app-exclude/data-factory/applicationTypeEqualConstantCodeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-exclude/data-factory/addRegistrationBody.json")
        .response("{}")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-exclude/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData(
        "/json/create-app-exclude/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/addApplicationFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .formKey("create-app-exclude-bp-check-compliance")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addApplicationFormActivity.json"))
        .expectedVariables(Map.of("addApplicationFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/checkComplianceFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionExcludeFormActivity")
        .formKey("create-app-exclude-bp-add-decision-exclude")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addDecisionExcludeFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("checkComplianceFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionExcludeFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/addDecisionExcludeFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLetterDataFormForExclusionActivity")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addLetterDataFormForExclusionActivityPrePopulation.json"))
        .expectedVariables(Map.of("addDecisionExcludeFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLetterDataFormForExclusionActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-exclude/form-data/addLetterDataFormForExclusionActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppExcludeFormActivity")
        .formKey("create-app-exclude-bp-sign-app-exclude")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/signAppExcludeFormActivity.json"))
        .expectedVariables(Map.of("addLetterDataFormForExclusionActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppExcludeFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/signAppExcludeFormActivity.json")
        .build());

    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addApplicationFormActivity", "checkComplianceFormActivity",
            "addDecisionExcludeFormActivity", "addLetterDataFormForExclusionActivity",
            "signAppExcludeFormActivity")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-exclude/dso/digitalSignatureCephContent.json");
  }

  @Test
  @Deployment(resources = {"bpmn/create-app-exclude.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void testPathWithMistakes() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/data-factory/lastLaboratorySolutionResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/applicationTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/solutionTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(laboratoryId)
        .response("/json/create-app-exclude/data-factory/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "WO_CONSIDER"))
        .response(
            "/json/create-app-exclude/data-factory/solutionTypeEqualConstantCodeWoConsiderResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "EXCLUDE"))
        .response(
            "/json/create-app-exclude/data-factory/applicationTypeEqualConstantCodeResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("registration")
        .requestBody("/json/create-app-exclude/data-factory/addRegistrationNoConsiderBody.json")
        .response("{}")
        .build());
    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/create-app-exclude/dso/digitalSignatureNoConsiderRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormData = deserializeFormData(
        "/json/create-app-exclude/form-data/startFormDataActivity.json");
    var processInstanceId = startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_KEY,
        testUserToken, startFormData);
    var processInstance = processInstance(processInstanceId);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .formKey("shared-add-application")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addApplicationFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addApplicationFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/addApplicationFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .formKey("create-app-exclude-bp-check-compliance")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addApplicationFormActivity.json"))
        .expectedVariables(Map.of("addApplicationFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("checkComplianceFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-exclude/form-data/noConsiderCheckComplianceFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionDenyFormActivity")
        .formKey("shared-add-decision-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addDecisionDenyFormActivityPrePopulation.json"))
        .expectedVariables(Map.of("checkComplianceFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addDecisionDenyFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/addDecisionDenyFormActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLetterDataFormForDenyActivity")
        .formKey("shared-add-letter-data")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/addLetterDataFormForDenyActivityPrePopulation.json"))
        .expectedVariables(Map.of("addDecisionDenyFormActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("addLetterDataFormForDenyActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/create-app-exclude/form-data/addLetterDataFormForDenyActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppDenyFormActivity")
        .formKey("create-app-exclude-bp-sign-app-deny")
        .assignee(testUserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/create-app-exclude/form-data/signAppDenyFormActivity.json"))
        .expectedVariables(Map.of("addLetterDataFormForDenyActivity_completer", testUserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(processInstanceId)
        .activityDefinitionId("signAppDenyFormActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/create-app-exclude/form-data/signAppDenyFormActivity.json")
        .build());

    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addApplicationFormActivity", "checkComplianceFormActivity",
            "addDecisionDenyFormActivity", "addLetterDataFormForDenyActivity",
            "signAppDenyFormActivity")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/create-app-exclude/dso/digitalSignatureNoConsiderCephContent.json");
  }

  @Test
  @Deployment(resources = "bpmn/create-app-exclude.bpmn")
  public void testValidationError() throws IOException {
    var laboratoryId = "d2943186-0f1f-4a77-9de9-a5a59c07db02";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("last-laboratory-solution")
        .queryParams(Maps.of("laboratoryId", laboratoryId))
        .response("/json/create-app-exclude/data-factory/lastLaboratorySolutionDenyResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("application-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/applicationTypeEqualConstantCodeAddResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("solution-type-equal-constant-code")
        .queryParams(Maps.of("constantCode", "ADD"))
        .response(
            "/json/create-app-exclude/data-factory/solutionTypeEqualConstantCodeAddResponse.json")
        .build());

    var startFormData = deserializeFormData(
        "/json/create-app-exclude/form-data/startFormDataActivity.json");
    var resultMap = startProcessInstanceWithStartFormForError(PROCESS_DEFINITION_KEY, testUserToken,
        startFormData);

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", "laboratory"),
        Map.entry("message", "Заява на видалення вже створена"),
        Map.entry("value", laboratoryId));
  }
}
