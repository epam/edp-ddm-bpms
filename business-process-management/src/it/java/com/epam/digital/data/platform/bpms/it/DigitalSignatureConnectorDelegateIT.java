package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class DigitalSignatureConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureConnectorDelegate.bpmn"})
  public void testDigitalSignatureConnectorDelegate() {
    formDataCephService.putFormData("cephKey", FormDataDto.builder()
        .accessToken("token").build());

    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\":\"data to sign\"}"))
            .willReturn(aResponse().withStatus(200).withBody("{\"signature\": \"test\"}"))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureConnectorDelegate_key", variables);
    assertThat(processInstance.isEnded()).isTrue();
  }
}
