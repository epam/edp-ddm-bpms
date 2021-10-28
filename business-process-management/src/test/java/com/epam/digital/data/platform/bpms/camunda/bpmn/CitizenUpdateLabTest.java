package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenUpdateLabTest extends BaseBpmnTest {

  private final String taskDispatcherUserName = "taskdispatcher";
  private String taskDispatcherToken;

  private final String testuser2UserName = "testuser2";
  private String testuser2Token;

  @Before
  public void setUp() {
    taskDispatcherToken = TestUtils.getContent("/json/taskDispatcherAccessToken.json");
    testuser2Token = TestUtils.getContent("/json/testuser2AccessToken.json");
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void happyPathTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-update-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "labToUpdateKoatuu"))
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Map.of("edrpou", "01010101", "name", "updatedLabName"))
        .response("[]")
        .build());
    mockGetKeycloakUsersConnectorDelegate("[]");
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .requestBody("/json/citizen-update-lab/dso/dsoRequest_nameUnique.json")
        .response("{\"signature\": \"systemSignature\"}")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .requestBody("/json/citizen-update-lab/data-factory/updatedLaboratoryRequest_name.json")
        .response("{}")
        .build());

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    startProcessInstanceWithStartForm("citizen-update-lab", startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .expectedVariables(Map.of("updateLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/signLabDataCitizenActivity_name.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-update-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_nameUnique.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .formKey("shared-officer-sign-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_nameUnique.json"))
        .expectedVariables(Map.of("checkLabOfficerActivity_completer", testuser2UserName))
        .extensionElements(Map.of("eSign", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("signLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/signLabDataOfficerActivity_nameUnique.json")
        .build());

    assertSystemSignature("system_signature_ceph_key",
        "/json/citizen-update-lab/dso/dsoCephContent_nameUnique.json");

    addExpectedVariable("signLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію оновлені");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void labNotUniqueTest() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-update-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "labToUpdateKoatuu"))
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    mockGetKeycloakUsersConnectorDelegate("[]");

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    startProcessInstanceWithStartForm("citizen-update-lab", startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json"))
        .expectedVariables(Map.of("updateLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/signLabDataCitizenActivity_accreditationFlag.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList()))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-update-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_accreditationFlag.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData(
            "/json/citizen-update-lab/form-data/checkLabDataOfficerActivity_notUnique.json")
        .build());

    addExpectedVariable("checkLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про лабораторію не оновлені - Така лабораторія вже існує");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void labDuplicateValidationException() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-update-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("laboratoryToUpdate")
        .response("/json/citizen-update-lab/data-factory/laboratoryToUpdate.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "labToUpdateKoatuu"))
        .response("/json/citizen-update-lab/data-factory/koatuuEqualKoatuuIdName.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("labToUpdateKoatuu")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKoatuu.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("labToUpdateOwnership")
        .response("/json/citizen-update-lab/data-factory/labToUpdateOwnership.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("labToUpdateKopfg")
        .response("/json/citizen-update-lab/data-factory/labToUpdateKopfg.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Map.of("edrpou", "01010101", "name", "updatedLabName"))
        .response("[{\"cnt\":1}]")
        .build());

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    startProcessInstanceWithStartForm("citizen-update-lab", startFormData);

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .formKey("read-lab-data-bp-view-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("viewLabDataCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/viewLabDataCitizenActivity.json"))
        .expectedVariables(Map.of("viewLabDataCitizenActivity_completer", "testuser",
            "laboratoryId", "laboratoryToUpdate"))
        .build());
    var ex = assertThrows(ValidationException.class,
        () -> completeTask(CompleteActivityDto.builder()
            .processInstanceId(currentProcessInstanceId)
            .activityDefinitionId("updateLabCitizenActivity")
            .completerUserName(testUserName)
            .completerAccessToken(testUserToken)
            .expectedFormData(
                "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json")
            .build()));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-update-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("updateLabCitizenActivity")
        .formKey("citizen-update-lab-bp-change-lab-data")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-update-lab/form-data/updateLabDataCitizenActivity_name.json"))
        .build());

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1).contains(
        new ErrorDetailDto("Дані про цю лабораторію вже присутні", "name", "updatedLabName"));
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void subjectDisabledValidationException() {
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject")
        .resourceId("activeSubject")
        .response("/json/common/data-factory/subjectResponse.json")
        .build());
    mockEdrResponse("/json/citizen-update-lab/edr/searchSubjectsDisabledResponse.json");

    var startFormData = deserializeFormData("/json/citizen-update-lab/form-data/start_event.json");
    var ex = assertThrows(ValidationException.class,
        () -> startProcessInstanceWithStartForm("citizen-update-lab", startFormData));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1).contains(
        new ErrorDetailDto("Суб'єкт скасовано або припинено", "", ""));
  }
}
