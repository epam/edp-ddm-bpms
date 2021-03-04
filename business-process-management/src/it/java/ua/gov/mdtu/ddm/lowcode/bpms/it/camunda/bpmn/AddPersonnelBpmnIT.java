package ua.gov.mdtu.ddm.lowcode.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.formDataVariableName;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.formDataVariableValue;
import static ua.gov.mdtu.ddm.lowcode.bpms.it.util.TestUtils.getContent;

import java.io.IOException;
import java.util.HashMap;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import ua.gov.mdtu.ddm.lowcode.bpms.it.builder.StubData;

public class AddPersonnelBpmnIT extends BaseBpmnIT {

  @Value("${camunda.system-variables.const_dataFactoryBaseUrl}")
  private String dataFactoryBaseUrl;

  @Test
  @Deployment(resources = {"bpmn/add-personnel.bpmn"})
  public void test() throws IOException {
    var labId = "bb652d3f-a36f-465a-b7ba-232a5a1680c5";
    var koatuuId = "bb652d3f-a36f-465a-b7ba-232a5a1680c4";

    stubDataFactoryGet(StubData.builder()
        .resource("laboratory")
        .resourceId(labId)
        .response("/json/add-personnel/data-factory/findLaboratoryResponse.json")
        .build());
    stubDataFactoryGet(StubData.builder()
        .resource("koatuu")
        .resourceId(koatuuId)
        .response("/json/add-personnel/data-factory/findKoatuuResponse.json")
        .build());

    stubDigitalSignature(StubData.builder()
        .requestBody("/json/add-personnel/dso/systemSignatureRequest.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryCreate(StubData.builder()
        .resource("staff")
        .requestBody("/json/add-personnel/data-factory/createStaffRequest.json")
        .response("{}")
        .build());

    var processInstance = runtimeService().startProcessInstanceByKey("add-personnel");
    assertThat(processInstance).isStarted();

    var processInstanceId = processInstance.getId();
    String initiator = null;

    var searchLabFormActivityDefinitionKey = "searchLabFormActivity";
    var searchLabFormRefVarName = formDataVariableName(searchLabFormActivityDefinitionKey);
    var searchLabFormCephKey = formDataVariableValue(processInstanceId, searchLabFormRefVarName);

    var viewLabDataFormActivityDefinitionKey = "viewLabDataFormActivity";
    var viewLabDataFormRefVarName = formDataVariableName(viewLabDataFormActivityDefinitionKey);
    var viewLabDataFormCephKey = formDataVariableValue(processInstanceId,
        viewLabDataFormRefVarName);

    var addPersonnelFormActivityDefinitionKey = "addPersonnelFormActivity";
    var addPersonnelFormRefVarName = formDataVariableName(addPersonnelFormActivityDefinitionKey);
    var addPersonnelFormCephKey = formDataVariableValue(processInstanceId,
        addPersonnelFormRefVarName);

    var signPersonnelFormActivityDefinitionKey = "signPersonnelFormActivity";
    var signPersonnelFormRefVarName = formDataVariableName(signPersonnelFormActivityDefinitionKey);
    var signPersonnelFormCephKey = formDataVariableValue(processInstanceId,
        signPersonnelFormRefVarName);

    var systemSignatureCephKeyRefVarName = "system_signature_ceph_key";
    var systemSignatureCephKey = "lowcode_" + processInstanceId + "_" +
        systemSignatureCephKeyRefVarName;

    var expectedVariablesMap = new HashMap<String, Object>();
    var expectedCephStorage = new HashMap<String, String>();

    expectedVariablesMap.put("initiator", initiator);
    expectedVariablesMap.put("const_dataFactoryBaseUrl", dataFactoryBaseUrl);
    //search lab task
    assertThat(processInstance).isWaitingAt(searchLabFormActivityDefinitionKey);
    assertThat(task(searchLabFormActivityDefinitionKey)).hasFormKey("shared-search-lab");
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent(expectedCephStorage);

    completeTask(searchLabFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/searchLabFormActivity.json");

    expectedVariablesMap.put(searchLabFormRefVarName, searchLabFormCephKey);
    expectedVariablesMap.put("laboratoryId", labId);
    expectedVariablesMap.put("koatuuId", koatuuId);
    expectedVariablesMap.put(viewLabDataFormRefVarName, viewLabDataFormCephKey);

    expectedCephStorage.put(searchLabFormCephKey,
        getContent("/json/add-personnel/form-data/searchLabFormActivity.json"));
    expectedCephStorage.put(viewLabDataFormCephKey,
        getContent("/json/add-personnel/form-data/viewLabDataFormActivityPrepopulation.json"));

    //view lab data task
    assertThat(processInstance).isWaitingAt(viewLabDataFormActivityDefinitionKey);
    assertThat(task(viewLabDataFormActivityDefinitionKey)).hasFormKey("shared-view-lab-data");
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent(expectedCephStorage);

    completeTask(viewLabDataFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/viewLabDataFormActivity.json");

    expectedVariablesMap.put(addPersonnelFormRefVarName, addPersonnelFormCephKey);

    expectedCephStorage.put(viewLabDataFormCephKey,
        getContent("/json/add-personnel/form-data/viewLabDataFormActivity.json"));
    expectedCephStorage.put(addPersonnelFormCephKey,
        getContent("/json/add-personnel/form-data/addPersonnelFormActivityPrepopulation.json"));

    assertThat(processInstance).isWaitingAt(addPersonnelFormActivityDefinitionKey);
    assertThat(task(addPersonnelFormActivityDefinitionKey))
        .hasFormKey("add-personnel-bp-add-personnel");
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent(expectedCephStorage);

    completeTask(addPersonnelFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/addPersonnelFormActivity.json");

    expectedVariablesMap.put(signPersonnelFormRefVarName, signPersonnelFormCephKey);

    expectedCephStorage.put(addPersonnelFormCephKey,
        getContent("/json/add-personnel/form-data/addPersonnelFormActivity.json"));
    expectedCephStorage.put(signPersonnelFormCephKey,
        getContent("/json/add-personnel/form-data/addPersonnelFormActivity.json"));

    assertThat(processInstance).isWaitingAt(signPersonnelFormActivityDefinitionKey);
    assertThat(task(signPersonnelFormActivityDefinitionKey))
        .hasFormKey("add-personnel-bp-sign-personnel");
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent(expectedCephStorage);

    completeTask(signPersonnelFormActivityDefinitionKey, processInstanceId,
        "/json/add-personnel/form-data/signPersonnelFormActivity.json");

    expectedVariablesMap.put(systemSignatureCephKeyRefVarName, systemSignatureCephKey);
    expectedVariablesMap
        .put("sys-var-process-completion-result", "Дані про кадровий склад внесені");

    expectedCephStorage.put(signPersonnelFormCephKey,
        getContent("/json/add-personnel/form-data/signPersonnelFormActivity.json"));
    expectedCephStorage.put(systemSignatureCephKey,
        getContent("/json/add-personnel/dso/systemSignatureCephContent.json"));

    assertThat(processInstance).isEnded();
    assertThat(processInstance).variables().hasSize(18).containsAllEntriesOf(expectedVariablesMap);
    assertCephContent(expectedCephStorage);
  }
}
