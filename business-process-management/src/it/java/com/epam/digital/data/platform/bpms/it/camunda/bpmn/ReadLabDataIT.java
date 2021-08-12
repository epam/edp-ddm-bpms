package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class ReadLabDataIT extends BaseBpmnIT {

  @Test
  public void happyPath() throws IOException {
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("laboratory")
        .resourceId("d2943186-0f1f-4a77-9de9-a5a59c07db02")
        .response("/json/read-lab/laboratoryByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322996")
        .response("/json/read-lab/koatuuByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu-equal-koatuu-id-name")
        .queryParams(Map.of("koatuuId", "92cb1462-ec57-4b87-9e8d-594e0c322996"))
        .response("/json/read-lab/koatuuEqualKoatuuIdName.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("koatuu")
        .resourceId("92cb1462-ec57-4b87-9e8d-594e0c322997")
        .response("/json/read-lab/koatuuOblByIdResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("ownership")
        .resourceId("19aab23b-1e49-4064-8f7e-39735ece4388")
        .response("/json/read-lab/findOwnershipResponse.json")
        .build());
    stubDataFactoryRequest(StubData.builder()
        .httpMethod(HttpMethod.GET)
        .headers(Map.of("X-Access-Token", testUserToken))
        .resource("kopfg")
        .resourceId("a790eb71-6015-4f40-995b-ad474e8eddca")
        .response("/json/read-lab/findKopfgResponse.json")
        .build());

    //start process
    var startFormCephKey = "startFormCephKey";
    var data = new LinkedHashMap<String, Object>();
    data.put("laboratory", Map.of("laboratoryId", "d2943186-0f1f-4a77-9de9-a5a59c07db02"));
    cephService.putFormData(startFormCephKey, FormDataDto.builder().data(data).build());

    var processInstanceId = startProcessInstanceWithStartFormAndGetId("read-lab",
        startFormCephKey, testUserToken);
    var processInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    BpmnAwareTests.assertThat(processInstance).isWaitingAt("viewLabDataFormActivity");

    completeTask("viewLabDataFormActivity", processInstanceId, "{}");

    BpmnAwareTests.assertThat(processInstance).hasPassed("viewLabDataFormActivity").isEnded();
    assertThat(processInstance).variables().containsKey("sys-var-process-completion-result");
    assertThat(processInstance).variables().containsValue("Дані про лабораторію відображені");
  }

}
