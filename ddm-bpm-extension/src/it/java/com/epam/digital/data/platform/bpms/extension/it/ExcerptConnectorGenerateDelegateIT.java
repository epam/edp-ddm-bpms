/*
 * Copyright 2021 EPAM Systems.
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

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.dso.api.dto.SignRequestDto;
import com.epam.digital.data.platform.dso.api.dto.SignResponseDto;
import com.epam.digital.data.platform.excerpt.model.ExcerptEntityId;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExcerptConnectorGenerateDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/connector/testGenerateExcerpt.bpmn")
  public void shouldGenerateExcerpt() throws Exception {
    var requestDto = new ExcerptEventDto();
    requestDto.setExcerptType("subject-laboratories-accreditation-excerpt");
    requestDto.setExcerptInputData(Map.of("subjectId", "1234"));
    requestDto.setRequiresSystemSignature(true);

    var requestBody = objectMapper.writeValueAsString(requestDto);

    var dsoResponse = SignResponseDto.builder().signature("sign").build();
    digitalSignatureMockServer.addStubMapping(
        stubFor(
            post(urlPathEqualTo("/api/eseal/sign"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(
                    equalTo(
                        objectMapper.writeValueAsString(
                            SignRequestDto.builder().data(requestBody).build())))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsString(dsoResponse)))));

    excerptServiceWireMock.addStubMapping(
        stubFor(
            post(urlPathEqualTo("/excerpt-mock-service/excerpts"))
                .withHeader(
                    PlatformHttpHeader.X_DIGITAL_SIGNATURE_DERIVED.getName(),
                    matching("lowcode_.*_system_signature_ceph_key"))
                .withRequestBody(equalTo(requestBody))
                .willReturn(
                    aResponse()
                        .withBody(
                            objectMapper.writeValueAsString(
                                new ExcerptEntityId(
                                    UUID.fromString("d564f2ab-eec6-11eb-9efa-0a580a820439"))))
                        .withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_generate_excerpt");

    var processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();

    var derivedSignatureCephKey = String.format(FormDataKeyProviderImpl.SYSTEM_SIGNATURE_STORAGE_KEY,
            processInstance.getId(),
            processInstance.getId());
    var signedFormData = formDataStorageService.getFormData(derivedSignatureCephKey);
    assertThat(signedFormData).isPresent();
    assertThat(signedFormData.get().getSignature()).isEqualTo(dsoResponse.getSignature());
  }
}
