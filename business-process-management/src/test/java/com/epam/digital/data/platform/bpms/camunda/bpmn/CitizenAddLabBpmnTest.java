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

public class CitizenAddLabBpmnTest extends BaseBpmnTest {

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
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void happyPathTest() {
    mockEdrResponse("/json/citizen-add-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .response("/json/citizen-add-lab/data-factory/searchSubjectResponse.json")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .build());
    mockGetKeycloakUsersConnectorDelegate("[]");
    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .requestBody("/json/citizen-add-lab/dso/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    startProcessInstance("citizen-add-lab", Map.of("initiator", testUserName));

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testuser2Token))
        .headers(Map.of("X-Digital-Signature",
            cephKeyProvider.generateKey("signLabOfficerActivity", currentProcessInstanceId)))
        .resource("laboratory")
        .requestBody("/json/citizen-add-lab/data-factory/addLabRequestBody.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("addLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList(), "subjectId", "activeSubject"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-add-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/checkLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/checkLabOfficerActivityUnique.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabOfficerActivity")
        .formKey("shared-officer-sign-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("checkLabOfficerActivity_completer", testuser2UserName))
        .extensionElements(Map.of("eSign", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("signLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabOfficerActivityUnique.json")
        .build());

    addExpectedVariable("signLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторія створена");

    assertSystemSignature("system_signature_ceph_key",
        "/json/citizen-add-lab/dso/digitalSignatureCephContent.json");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void duplicateLabCheckFailedTest() {
    mockEdrResponse("/json/citizen-add-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("subject-equal-subject-type-equal-subject-code")
        .response("/json/citizen-add-lab/data-factory/searchSubjectResponse.json")
        .queryParams(Map.of("subjectType", "LEGAL", "subjectCode", "01010101"))
        .build());
    mockGetKeycloakUsersConnectorDelegate("[]");

    startProcessInstance("citizen-add-lab", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("addLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("signLabCitizenActivity")
        .formKey("shared-citizen-sign-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/signLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("addLabCitizenActivity_completer", "testuser"))
        .extensionElements(Map.of("eSign", "true", "ENTREPRENEUR", "true", "LEGAL", "true"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("signLabCitizenActivity")
        .completerUserName(testUserName)
        .completerAccessToken(testUserToken)
        .expectedFormData("/json/citizen-add-lab/form-data/signLabCitizenActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("dispatchTaskActivity")
        .formKey("shared-dispatch-task")
        .candidateRoles(List.of("task-dispatcher"))
        .extensionElements(Map.of("formVariables", "officerUsers"))
        .expectedVariables(Map.of("signLabCitizenActivity_completer", "testuser",
            "officerUsers", Collections.emptyList(), "subjectId", "activeSubject"))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("dispatchTaskActivity")
        .completerUserName(taskDispatcherUserName)
        .completerAccessToken(taskDispatcherToken)
        .expectedFormData("/json/citizen-add-lab/form-data/dispatchTaskActivity.json")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("checkLabOfficerActivity")
        .formKey("shared-officer-check-lab")
        .assignee(testuser2UserName)
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/checkLabOfficerActivityPrePopulation.json"))
        .expectedVariables(Map.of("dispatchTaskActivity_completer", taskDispatcherUserName,
            "officerAssignee", testuser2UserName))
        .build());
    completeTask(CompleteActivityDto.builder()
        .activityDefinitionId("checkLabOfficerActivity")
        .completerUserName(testuser2UserName)
        .completerAccessToken(testuser2Token)
        .expectedFormData("/json/citizen-add-lab/form-data/checkLabOfficerActivityDuplicate.json")
        .build());

    addExpectedVariable("checkLabOfficerActivity_completer", testuser2UserName);
    addExpectedVariable(ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Лабораторія не створена - Така лабораторія вже існує");

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void duplicateLabTest() {
    mockEdrResponse("/json/citizen-add-lab/edr/searchSubjectsActiveResponse.json");
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .response("[{\"cnt\":1}]")
        .queryParams(Map.of("edrpou", "01010101", "name", "labName"))
        .build());

    startProcessInstance("citizen-add-lab", Map.of("initiator", testUserName));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(deserializeFormData(
            "/json/citizen-add-lab/form-data/addLabCitizenActivityPrePopulation.json"))
        .expectedVariables(Map.of("initiator", "testuser"))
        .build());

    var ex = assertThrows(ValidationException.class,
        () -> completeTask(CompleteActivityDto.builder()
            .activityDefinitionId("addLabCitizenActivity")
            .completerUserName(testUserName)
            .completerAccessToken(testUserToken)
            .expectedFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json")
            .build()));

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey("citizen-add-lab")
        .processInstanceId(currentProcessInstanceId)
        .activityDefinitionId("addLabCitizenActivity")
        .formKey("citizen-add-lab-bp-add-lab")
        .assignee("testuser")
        .expectedFormDataPrePopulation(
            deserializeFormData("/json/citizen-add-lab/form-data/addLabCitizenActivity.json"))
        .build());

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1)
        .contains(new ErrorDetailDto("Дані про цю лабораторію вже присутні", "name", "labName"));
    mockServer.verify();
  }

  @Test
  @Deployment(resources = {"bpmn/citizen-add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void disabledSubjectTest() {
    mockEdrResponse("/json/citizen-add-lab/edr/searchSubjectsDisabledResponse.json");

    var ex = assertThrows(ValidationException.class,
        () -> startProcessInstance("citizen-add-lab"));

    Assertions.assertThat(ex.getDetails().getErrors()).hasSize(1)
        .contains(new ErrorDetailDto("Суб'єкт скасовано або припинено", "", ""));
  }
}
