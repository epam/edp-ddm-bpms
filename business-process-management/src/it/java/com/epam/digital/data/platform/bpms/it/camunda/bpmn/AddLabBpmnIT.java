package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AddLabBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/add-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "77777777"))
        .response("[]")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/add-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    //start process
    var processInstanceId = startProcessInstanceAndGetId();
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("laboratory")
        .headers(Map.of("X-Access-Token", testUserToken, "X-Digital-Signature",
            cephKeyProvider.generateKey("signLabFormActivity", processInstanceId)))
        .requestBody("/json/add-lab/addLabRequestBody.json")
        .response("{}")
        .build());

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("addLabFormActivity");
    completeTask("addLabFormActivity", processInstanceId, "/json/add-lab/addLabFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signLabFormActivity");
    completeTask("signLabFormActivity", processInstanceId,
        "/json/add-lab/signLabFormActivity.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("addLabFormActivity", "signLabFormActivity").isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/add-lab/digitalSignatureCephContent.json");
  }

  private String startProcessInstanceAndGetId() throws JsonProcessingException {
    saveStartFormDataToCeph();
    return startProcessInstanceWithStartFormAndGetId("add-lab", START_FORM_CEPH_KEY,
        testUserToken);
  }

  private void saveStartFormDataToCeph() {
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", "LEGAL");
    data.put("edrpou", "77777777");
    data.put("subject", Map.of("subjectId", "activeSubject"));
    cephService.putFormData(START_FORM_CEPH_KEY, FormDataDto.builder().data(data).build());
  }
}