/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.service;

import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dso.api.dto.SignRequestDto;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.context.DelegateExecutionContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class DigitalSystemSignatureService {

  private final DigitalSealRestClient digitalSealRestClient;
  private final HeaderBuilderFactory headerBuilderFactory;
  private final ObjectMapper objectMapper;
  private final FormDataStorageService formDataStorageService;
  private final FormDataKeyProvider formDataKeyProvider;

  public String sign(SystemSignatureDto systemSignatureDto)
      throws JsonProcessingException {
    log.debug("Start sending data to sign");

    var data = systemSignatureDto.getPayload().toString();
    var reqBody = SignRequestDto.builder().data(data).build();
    var headers = headerBuilderFactory.builder().contentTypeJson().accessTokenHeader().build();
    var response = digitalSealRestClient.sign(reqBody, headers);
    log.debug("Got digital signature");

    var signedFormData =
        FormDataDto.builder()
            .data(objectMapper.readValue(data, new TypeReference<>() {}))
            .signature(response.getSignature())
            .build();

    var storageKey = generateStorageKey(systemSignatureDto);

    log.debug("Start putting signature to storage");
    formDataStorageService.putFormData(storageKey, signedFormData);
    log.debug("Signature put successfully");
    return storageKey;
  }

  private String generateStorageKey(
      SystemSignatureDto systemSignatureDto) {
    var execution = DelegateExecutionContext.getCurrentDelegationExecution();
    var index = systemSignatureDto.getIndex();
    if (Objects.nonNull(index)) {
      return formDataKeyProvider.generateBatchSystemSignatureKey(
          execution.getProcessInstanceId(), index);
    }
    return formDataKeyProvider.generateSystemSignatureKey(
        ((ExecutionEntity) execution).getRootProcessInstanceId(), execution.getProcessInstanceId());
  }

  @Data
  @Builder
  public static class SystemSignatureDto {
    private SpinJsonNode payload;
    private Integer index;
  }
}
