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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dso.api.dto.SignRequestDto;
import com.epam.digital.data.platform.dso.client.DigitalSealRestClient;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for digital
 * signature of data
 */
@Slf4j
@RequiredArgsConstructor
@Component(DigitalSystemSignatureDelegate.DELEGATE_NAME)
public class DigitalSystemSignatureDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "digitalSystemSignatureDelegate";

  @Getter
  @SystemVariable(name = "system_signature_storage_key")
  private NamedVariableAccessor<String> systemSignatureStorageKey;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "payloadIndex", isTransient = true)
  protected NamedVariableAccessor<Integer> payloadIndex;

  private final DigitalSealRestClient digitalSealRestClient;
  private final HeaderBuilderFactory headerBuilderFactory;
  private final ObjectMapper objectMapper;
  private final FormDataStorageService formDataStorageService;
  private final FormDataKeyProvider formDataKeyProvider;

  @Override
  public void executeInternal(DelegateExecution execution) throws JsonProcessingException {
    var payload = payloadVariable.from(execution).get();

    log.debug("Start sending data to sign");

    if (Objects.isNull(payload)) {
      log.debug("Payload is null");
      return;
    }
    var data = payload.toString();
    var reqBody = SignRequestDto.builder().data(data).build();
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .accessTokenHeader()
        .build();
    var response = digitalSealRestClient.sign(reqBody, headers);
    log.debug("Got digital signature");

    var signedFormData = FormDataDto.builder()
        .data(objectMapper.readValue(data, new TypeReference<>() {
        }))
        .signature(response.getSignature())
        .build();

    var storageKey = generateStorageKey(execution);

    log.debug("Start putting signature to storage");
    formDataStorageService.putFormData(storageKey, signedFormData);
    log.debug("Signature put successfully");

    systemSignatureStorageKey.on(execution).set(storageKey);
  }

  private String generateStorageKey(DelegateExecution execution) {
    var index = payloadIndex.from(execution).get();
    if (Objects.nonNull(index)) {
      return formDataKeyProvider.generateBatchSystemSignatureKey(execution.getProcessInstanceId(),
          index);
    }
    return formDataKeyProvider.generateSystemSignatureKey(
        ((ExecutionEntity) execution).getRootProcessInstanceId(), execution.getProcessInstanceId());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
