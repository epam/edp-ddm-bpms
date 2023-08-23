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
import com.epam.digital.data.platform.bpms.extension.service.DigitalSystemSignatureService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for digital
 * signature of data
 */
@Slf4j
@RequiredArgsConstructor
@Component(DigitalSystemSignatureDelegate.DELEGATE_NAME)
public class DigitalSystemSignatureDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "digitalSystemSignatureDelegate";

  @SystemVariable(name = "system_signature_storage_key")
  private NamedVariableAccessor<String> systemSignatureStorageKey;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;

  private final DigitalSystemSignatureService digitalSystemSignatureService;

  @Override
  public void executeInternal(DelegateExecution execution) throws JsonProcessingException {
    systemSignatureStorageKey.on(execution).set("");

    var payload = payloadVariable.from(execution).get();

    if (Objects.isNull(payload)) {
      log.debug("Payload is null");
      return;
    }

    var systemSignatureDto =
        DigitalSystemSignatureService.SystemSignatureDto.builder().payload(payload).build();
    var storageKey = digitalSystemSignatureService.sign(systemSignatureDto);

    systemSignatureStorageKey.on(execution).set(storageKey);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
