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
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for digital
 * signature of data
 */
@Slf4j
@RequiredArgsConstructor
@Component(DigitalSignatureConnectorDelegate.DELEGATE_NAME)
public class DigitalSignatureConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "digitalSignatureConnectorDelegate";
  public static final String PROP_DATA = "data";

  @Getter
  @SystemVariable(name = "response", isTransient = true)
  private NamedVariableAccessor<SpinJsonNode> dsoResponseVariable;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;

  private final DigitalSealRestClient digitalSealRestClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws JsonProcessingException {
    var payload = payloadVariable.from(execution).get();

    log.debug("Start sending data to sign");

    if (Objects.isNull(payload) || !payload.hasProp(PROP_DATA)){
      log.debug("Payload is null or property 'data' is missing");
      return;
    }
    var data = payload.prop("data").stringValue();
    var reqBody = SignRequestDto.builder().data(data).build();
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .accessTokenHeader()
        .build();
    var response = digitalSealRestClient.sign(reqBody, headers);
    log.debug("Got digital signature");

    dsoResponseVariable.on(execution).set(SpinJsonNode.JSON(response));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
