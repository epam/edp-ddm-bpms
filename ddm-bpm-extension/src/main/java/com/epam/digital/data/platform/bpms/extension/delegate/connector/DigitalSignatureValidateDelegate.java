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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dso.api.dto.SignFormat;
import com.epam.digital.data.platform.dso.api.dto.SignInfoRequestDto;
import com.epam.digital.data.platform.dso.api.dto.ValidationResponseDto;
import com.epam.digital.data.platform.dso.client.DigitalSignatureRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to validate
 * signed data.
 */
@Slf4j
@RequiredArgsConstructor
@Component(DigitalSignatureValidateDelegate.DELEGATE_NAME)
public class DigitalSignatureValidateDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "digitalSignatureValidateDelegate";

  @SystemVariable(name = "signedData", isTransient = true)
  private NamedVariableAccessor<String> signedDataVariable;
  @SystemVariable(name = "containerType", isTransient = true)
  private NamedVariableAccessor<String> containerTypeVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ValidationResponseDto> responseVariable;

  private final DigitalSignatureRestClient digitalSignatureRestClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    responseVariable.on(execution).set(new ValidationResponseDto());

    var signedData = signedDataVariable.from(execution).getOrThrow();
    var containerType = containerTypeVariable.from(execution).getOrThrow();
    var container = SignFormat.valueOf(containerType);
    log.debug("Start validation with container type - {}", container);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .accessTokenHeader()
        .build();
    var reqBody = SignInfoRequestDto.builder().data(signedData).container(container).build();
    var validationResult = digitalSignatureRestClient.validate(reqBody, headers);
    log.debug("Validation finished, result - {}", validationResult.isValid());

    responseVariable.on(execution).set(validationResult);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
