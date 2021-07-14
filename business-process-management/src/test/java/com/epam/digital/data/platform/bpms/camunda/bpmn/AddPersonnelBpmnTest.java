package com.epam.digital.data.platform.bpms.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddPersonnelBpmnTest extends BaseBpmnTest {

  @Test
  @Deployment(resources = {"bpmn/add-personnel.bpmn"})
  public void test() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    var koatuuId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";
    var ownershipId = "60c99b70-3644-4938-b785-027c25c13c87";
    var kopfgId = "c54af316-4280-43fd-b49a-1e1f44ac374b";

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/add-personnel/data-factory/findLaboratoryResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId(koatuuId)
        .response("/json/add-personnel/data-factory/findKoatuuResponse.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", koatuuId))
        .response("/json/add-personnel/data-factory/koatuuEqualKoatuuIdName.json")
        .build());

    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-obl-contains-name")
        .queryParams(Map.of("name", "KoatuuObl"))
        .response("/json/add-personnel/data-factory/koatuuOblContainsName.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId(ownershipId)
        .response("/json/add-personnel/data-factory/findOwnershipResponse.json")
        .build());
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId(kopfgId)
        .response("/json/add-personnel/data-factory/findKopfgResponse.json")
        .build());

    mockDigitalSignatureSign(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    startProcessInstance("add-personnel");

    var searchLabFormActivityDefinitionKey = "searchLabFormActivity";
    var viewLabDataFormActivityDefinitionKey = "viewLabDataFormActivity";
    var addPersonnelFormActivityDefinitionKey = "addPersonnelFormActivity";
    var signPersonnelFormActivityDefinitionKey = "signPersonnelFormActivity";

    var xDigitalSignature = cephKeyProvider
        .generateKey(signPersonnelFormActivityDefinitionKey, currentProcessInstanceId);
    mockDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken,
            "X-Digital-Signature", xDigitalSignature))
        .resource("staff")
        .requestBody("/json/add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + currentProcessInstanceId + "_" +
        systemSignatureCephKeyRefVarName + "_0";

    addExpectedVariable("initiator", null);
    //search lab task
    assertWaitingActivity(searchLabFormActivityDefinitionKey, "shared-search-lab");

    completeTask(searchLabFormActivityDefinitionKey,
        "/json/add-personnel/form-data/searchLabFormActivity.json");

    addExpectedVariable("laboratoryId", labId);
    addExpectedVariable("searchLabFormActivity_completer", testUserName);
    addExpectedCephContent(searchLabFormActivityDefinitionKey,
        "/json/add-personnel/form-data/searchLabFormActivity.json");
    addExpectedCephContent(viewLabDataFormActivityDefinitionKey,
        "/json/add-personnel/form-data/viewLabDataFormActivityPrepopulation.json");

    //view lab data task
    assertWaitingActivity(viewLabDataFormActivityDefinitionKey, "shared-view-lab-data");

    completeTask(viewLabDataFormActivityDefinitionKey,
        "/json/add-personnel/form-data/viewLabDataFormActivity.json");

    addExpectedVariable("viewLabDataFormActivity_completer", testUserName);
    addExpectedCephContent(viewLabDataFormActivityDefinitionKey,
        "/json/add-personnel/form-data/viewLabDataFormActivity.json");
    addExpectedCephContent(addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivityPrepopulation.json");

    assertWaitingActivity(addPersonnelFormActivityDefinitionKey,
        "add-personnel-bp-add-personnel");

    completeTask(addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivity.json");

    addExpectedVariable("addPersonnelFormActivity_completer", testUserName);
    addExpectedCephContent(addPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/addPersonnelFormActivity.json");
    addExpectedCephContent(signPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/signPersonnelFormActivityPrepopulation.json");

    assertWaitingActivity(signPersonnelFormActivityDefinitionKey,
        "add-personnel-bp-sign-personnel");

    completeTask(signPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/signPersonnelFormActivity.json");

    addExpectedVariable("x_digital_signature_derived_ceph_key", systemSignatureCephKey);
    addExpectedVariable("sys-var-process-completion-result", "Дані про кадровий склад внесені");

    addExpectedCephContent(signPersonnelFormActivityDefinitionKey,
        "/json/add-personnel/form-data/signPersonnelFormActivity.json");
    var signature = cephService.getContent(cephBucketName, systemSignatureCephKey).get();
    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(signature);
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent("/json/add-personnel/dso/systemSignatureCephContent.json"));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);

    assertThat(currentProcessInstance).isEnded();
    assertThat(currentProcessInstance).variables().containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();

    mockServer.verify();
  }
}
