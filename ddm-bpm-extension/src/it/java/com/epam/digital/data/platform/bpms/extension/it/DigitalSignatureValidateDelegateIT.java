/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.extension.exception.SignatureValidationException;
import com.epam.digital.data.platform.dso.api.dto.ErrorDto;
import com.epam.digital.data.platform.dso.api.dto.SignFormat;
import com.epam.digital.data.platform.dso.api.dto.ValidationResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class DigitalSignatureValidateDelegateIT extends BaseIT {

  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureValidateDelegate.bpmn"})
  public void shouldGetSuccessfulValidationResponse() throws JsonProcessingException {
    var response = new ValidationResponseDto(true, SignFormat.CADES, null);
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/validate"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\":\"dGVzdCBkYXRh\",\"container\":\"CADES\"}"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                    .withBody(objectMapper.writeValueAsString(response)))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureSuccessfulValidationResponse");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureValidateDelegate.bpmn"})
  public void shouldGetFailedValidationResponse() throws JsonProcessingException {
    var error = ErrorDto.builder().code("ERROR_PKI_FORMATS_FAILED").build();
    var response = new ValidationResponseDto(false, SignFormat.UNDEFINED, error);
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/validate"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\":\"dGVzdCBkYXRh\",\"container\":\"ASIC\"}"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                    .withBody(objectMapper.writeValueAsString(response)))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDigitalSignatureFailedValidationResponse");

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureValidateDelegate.bpmn"})
  public void shouldGetTechnicalErrorResponse() throws JsonProcessingException {
    var error = ErrorDto.builder()
        .code("ERROR_UNKNOWN")
        .message("Something went wrong")
        .build();
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/validate"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\":\"dGVzdCBkYXRh\",\"container\":\"ASIC\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(500)
                .withBody(objectMapper.writeValueAsString(error)))));

    var exception = assertThrows(SignatureValidationException.class,
        () -> runtimeService.startProcessInstanceByKey(
            "testDigitalSignatureFailedValidationResponse"));
    assertEquals("Something went wrong", exception.getMessage());
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDigitalSignatureValidateDelegate.bpmn"})
  public void shouldGetRuntimeErrorResponse() {
    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/esignature/validate"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo("{\"data\":\"dGVzdCBkYXRh\",\"container\":\"ASIC\"}"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(502)
                .withBody("{\"message\":\"Bad Gateway\"}"))));

    var exception = assertThrows(SignatureValidationException.class,
        () -> runtimeService.startProcessInstanceByKey(
            "testDigitalSignatureFailedValidationResponse"));
    assertThat(exception.getMessage(),
        matchesPattern("\\[502 Bad Gateway] .*"));
  }
}
