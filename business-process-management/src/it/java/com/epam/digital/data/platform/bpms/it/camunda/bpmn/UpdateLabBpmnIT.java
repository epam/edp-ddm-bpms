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

public class UpdateLabBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = {"bpmn/update-lab.bpmn", "bpmn/system-signature-bp.bpmn"})
  public void test() throws IOException {

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/update-lab/laboratoryByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322996")
        .response("/json/update-lab/koatuuByIdResponse.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory-equal-edrpou-name-count")
        .queryParams(Maps.of("name", "labName", "edrpou", "23510933"))
        .response("[]")
        .build());

    stubDigitalSignatureRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .headers(Map.of("X-Access-Token", testUserToken))
        .requestBody("/json/update-lab/digitalSignatureRequestBody.json")
        .response("{\"signature\": \"test\"}")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "92cb1462-ec57-4b87-9e8d-594e0c322996"))
        .response("/json/update-lab/koatuuEqualKoatuuIdName.json")
        .build());

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322997")
        .response("/json/update-lab/koatuuOblByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("19aab23b-1e49-4064-8f7e-39735ece4388")
        .response("/json/update-lab/findOwnershipResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("a790eb71-6015-4f40-995b-ad474e8eddca")
        .response("/json/update-lab/findKopfgResponse.json")
        .build());

    //start process
    var processInstanceId = startProcessInstanceAndGetId();
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).list().get(0);

    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.PUT)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .requestBody("/json/update-lab/updateLabRequestBody.json")
        .response("{}")
        .build());

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("viewLabDataFormActivity");
    completeTask("viewLabDataFormActivity", processInstanceId,
        "/json/update-lab/viewLabDataFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("updateLabFormActivity");
    completeTask("updateLabFormActivity", processInstanceId,
        "/json/update-lab/updateLabFormActivity.json");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("signLabFormActivity");
    completeTask("signLabFormActivity", processInstanceId,
        "/json/update-lab/signLabFormActivity.json");

    //then
    BpmnAwareTests.assertThat(processInstance)
        .hasPassed("viewLabDataFormActivity", "updateLabFormActivity")
        .isEnded();

    assertSystemSignature(processInstanceId, "system_signature_ceph_key",
        "/json/update-lab/digitalSignatureCephContent.json");
  }

  private String startProcessInstanceAndGetId() throws JsonProcessingException {
    var data = new LinkedHashMap<String, Object>();
    data.put("subjectType", "LEGAL");
    data.put("edrpou", "77777777");
    data.put("subject", Map.of("subjectId", "activeSubject"));
    data.put("laboratory", Map.of("laboratoryId", "d2943186-0f1f-4a77-9de9-a5a59c07db02"));
    return startProcessInstanceWithStartFormAndGetId("update-lab", testUserToken,
        FormDataDto.builder().data(data).build());
  }
}
