package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

public class AddPersonnelBpmnIT extends BaseBpmnIT {

  @Value("${camunda.system-variables.const_dataFactoryBaseUrl}")
  private String dataFactoryBaseUrl;

  @Test
  @Deployment(resources = {"bpmn/add-personnel.bpmn"})
  public void test() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/add-personnel/data-factory/findLaboratoryResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", labId));
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var processInstanceId = startProcessInstanceWithStartFormAndGetId("add-personnel",
        "startFormCephKey", testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signPersonnelFormActivity", processInstanceId)))
        .resource("staff")
        .requestBody("/json/add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    var addPersonnelFormActivityDefinitionKey = "addPersonnelFormActivity";
    var signPersonnelFormActivityDefinitionKey = "signPersonnelFormActivity";

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName + "_0";

    expectedVariablesMap.put("initiator", "testuser");
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    addExpectedCephContent(processInstanceId, addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivityPrepopulation.json");

    expectedVariablesMap.put("start_form_ceph_key", "startFormCephKey");
    expectedVariablesMap.put("laboratoryId", labId);
    assertWaitingActivity(processInstance, addPersonnelFormActivityDefinitionKey,
        "add-personnel-bp-add-personnel");

    completeTask(addPersonnelFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/addPersonnelFormActivity.json");

    addCompleterUsernameVariable(addPersonnelFormActivityDefinitionKey, testUserName);

    addExpectedCephContent(processInstanceId, addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivity.json");
    addExpectedCephContent(processInstanceId, signPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/signPersonnelFormActivityPrepopulation.json");

    assertWaitingActivity(processInstance, signPersonnelFormActivityDefinitionKey,
        "add-personnel-bp-sign-personnel");

    completeTask(signPersonnelFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/signPersonnelFormActivity.json");

    expectedVariablesMap.put("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    expectedVariablesMap
        .put("sys-var-process-completion-result", "Дані про кадровий склад внесені");
    addCompleterUsernameVariable(signPersonnelFormActivityDefinitionKey, testUserName);

    addExpectedCephContent(processInstanceId, signPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/signPersonnelFormActivity.json");

    String signature = cephService.getContent(cephBucketName, systemSignatureCephKey).get();
    Map<String, Object> signatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(signature);
    Map<String, Object> expectedSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent("/json/add-personnel/dso/systemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }
}
