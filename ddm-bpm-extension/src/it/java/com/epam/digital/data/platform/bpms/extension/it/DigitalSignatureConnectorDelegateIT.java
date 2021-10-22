package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.LinkedHashMap;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class DigitalSignatureConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureConnectorDelegate.bpmn"})
  public void testDigitalSignatureConnectorDelegate() {
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo(validAccessToken))
            .withRequestBody(equalTo("{\"data\":\"data to sign\"}"))
            .willReturn(aResponse().withStatus(200).withBody("{\"signature\": \"test\"}"))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureConnectorDelegate_key");

    var cephKey = cephKeyProvider
        .generateKey("testActivity", processInstance.getProcessInstanceId());
    cephService.putFormData(cephKey, FormDataDto.builder().accessToken(validAccessToken)
        .data(new LinkedHashMap<>()).build());

    String taskId = taskService.createTaskQuery().taskDefinitionKey("waitConditionTaskForDso").singleResult().getId();
    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }
}
