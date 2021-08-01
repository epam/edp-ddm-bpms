package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import java.io.IOException;
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
    var koatuuId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";
    var ownershipId = "60c99b70-3644-4938-b785-027c25c13c87";
    var kopfgId = "c54af316-4280-43fd-b49a-1e1f44ac374b";

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/add-personnel/data-factory/findLaboratoryResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId(koatuuId)
        .response("/json/add-personnel/data-factory/findKoatuuResponse.json")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", koatuuId))
        .response("/json/add-personnel/data-factory/koatuuEqualKoatuuIdName.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-obl-contains-name")
        .queryParams(Map.of("name", "KoatuuObl"))
        .response("/json/add-personnel/data-factory/koatuuOblContainsName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId(ownershipId)
        .response("/json/add-personnel/data-factory/findOwnershipResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId(kopfgId)
        .response("/json/add-personnel/data-factory/findKopfgResponse.json")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey("add-personnel");
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signPersonnelFormActivity", processInstanceId)))
        .resource("staff")
        .requestBody("/json/add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    String initiator = null;

    var searchLabFormActivityDefinitionKey = "searchLabFormActivity";
    var viewLabDataFormActivityDefinitionKey = "viewLabDataFormActivity";
    var addPersonnelFormActivityDefinitionKey = "addPersonnelFormActivity";
    var signPersonnelFormActivityDefinitionKey = "signPersonnelFormActivity";

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName + "_0";

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    //search lab task
    assertWaitingActivity(processInstance, searchLabFormActivityDefinitionKey,
        "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/searchLabFormActivity.json");

    expectedVariablesMap.put("laboratoryId", labId);
    addCompleterUsernameVariable(searchLabFormActivityDefinitionKey, testUserName);

    addExpectedCephContent(processInstanceId, searchLabFormActivityDefinitionKey,
        "/json/add-personnel/form-data/searchLabFormActivity.json");
    addExpectedCephContent(processInstanceId, viewLabDataFormActivityDefinitionKey,
        "/json/add-personnel/form-data/viewLabDataFormActivityPrepopulation.json");

    //view lab data task
    assertWaitingActivity(processInstance, viewLabDataFormActivityDefinitionKey,
        "shared-view-lab-data");

    completeTask(viewLabDataFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/viewLabDataFormActivity.json");

    addCompleterUsernameVariable(viewLabDataFormActivityDefinitionKey, testUserName);

    addExpectedCephContent(processInstanceId, viewLabDataFormActivityDefinitionKey,
        "/json/add-personnel/form-data/viewLabDataFormActivity.json");
    addExpectedCephContent(processInstanceId, addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivityPrepopulation.json");

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
