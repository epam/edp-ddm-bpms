package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.camunda.dto.AssertWaitingActivityDto;
import com.epam.digital.data.platform.bpms.camunda.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpms.camunda.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class CitizenAddPersonnelBpmnIT extends BaseBpmnIT {

  private static final String PROCESS_DEFINITION_ID = "citizen-add-personnel";

  @Test
  @Deployment(resources = "bpmn/citizen-add-personnel.bpmn")
  public void happyPathTest() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c8";

    stubSearchSubjects("/xml/citizen-add-personnel/searchSubjectsActiveResponse.xml");

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

    var processInstanceId = startProcessInstanceAndGetId(labId);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signCitizenPersonnelFormActivity", processInstanceId)))
        .resource("staff")
        .requestBody("/json/citizen-add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    CamundaAssertionUtil.assertWaitingActivity(AssertWaitingActivityDto.builder()
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
        .processDefinitionKey(PROCESS_DEFINITION_ID)
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
    addExpectedVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT,
        "Дані про кадровий склад внесені");

    assertSystemSignature(processInstanceId);
    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

  }

  @Test
  @Deployment(resources = "bpmn/citizen-add-personnel.bpmn")
  public void validationErrorTest() throws JsonProcessingException {
    stubSearchSubjects("/xml/citizen-add-personnel/searchSubjectsCancelledResponse.xml");

    var resultMap = startProcessInstanceForError();

    var errors = resultMap.get("details").get("errors");
    Assertions.assertThat(errors).hasSize(1);
    Assertions.assertThat(errors.get(0)).contains(Map.entry("field", ""),
        Map.entry("message", "Суб'єкт скасовано або припинено"),
        Map.entry("value", ""));

  }

  private void assertSystemSignature(String processInstanceId) throws JsonProcessingException {
    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName + "_0";
    var signature = cephService.getContent(cephBucketName, systemSignatureCephKey);
    Assertions.assertThat(signature).isNotEmpty();
    Map<String, Object> signatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(signature.get());
    Map<String, Object> expectedSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent("/json/citizen-add-personnel/dso/systemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);
  }

  private String startProcessInstanceAndGetId(String labId) throws JsonProcessingException {
    saveStartFormDataToCeph(labId);
    return startProcessInstanceWithStartFormAndGetId(PROCESS_DEFINITION_ID, START_FORM_CEPH_KEY,
        testUserToken);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceForError()
      throws JsonProcessingException {
    saveStartFormDataToCeph("bb652d3f-a36f-465a-b7ba-232a5a1680c8");
    var resultMap = startProcessInstanceWithStartForm(PROCESS_DEFINITION_ID,
        START_FORM_CEPH_KEY, testUserToken);
    return (Map<String, Map<String, List<Map<String, String>>>>) resultMap;
  }

  private void saveStartFormDataToCeph(String labId) {
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId));
    data.put("edrpou", "77777777");
    data.put("subjectType", "LEGAL");
    data.put("subject", Map.of("subjectId", "activeSubject"));
    cephService.putFormData(START_FORM_CEPH_KEY, FormDataDto.builder().data(data).build());
  }
}
