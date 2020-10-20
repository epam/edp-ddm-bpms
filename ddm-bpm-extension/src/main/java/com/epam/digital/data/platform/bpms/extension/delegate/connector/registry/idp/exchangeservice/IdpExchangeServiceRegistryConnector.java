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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.idp.exchangeservice;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.idp.exchangeservice.dto.IdmExchangeServiceResponseDto;
import com.epam.digital.data.platform.starter.trembita.integration.idp.exchangeservice.dto.IdpExchangeServiceRequestDto;
import com.epam.digital.data.platform.starter.trembita.integration.idp.exchangeservice.service.IdpExchangeRegistryService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;

@Slf4j
@RequiredArgsConstructor
public class IdpExchangeServiceRegistryConnector extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "idpExchangeServiceRegistryConnector";

  @SystemVariable(name = "url")
  private NamedVariableAccessor<String> urlVariable;
  @SystemVariable(name = "method")
  private NamedVariableAccessor<String> methodVariable;
  @SystemVariable(name = "body", isTransient = true)
  private NamedVariableAccessor<SpinJsonNode> bodyVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<RegistryConnectorResponse> responseVariable;

  private final IdpExchangeRegistryService idpExchangeRegistryService;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var url = urlVariable.from(execution).getOrThrow();
    var method = methodVariable.from(execution).getOrThrow();
    var body = bodyVariable.from(execution).get();

    log.debug("Start executing request to IdpExchangeServiceRegistry");
    var result = idpExchangeRegistryService.request(
        IdpExchangeServiceRequestDto.builder()
            .url(url)
            .method(method)
            .body(Objects.nonNull(body) ? body.toString() : null)
            .build()
    );
    log.debug("Got response from IdpExchangeServiceRegistry");
    responseVariable.on(execution).set(prepareConnectorResponse(result));
  }

  private RegistryConnectorResponse prepareConnectorResponse(
      IdmExchangeServiceResponseDto response) {
    var spin = Objects.isNull(response) ? null : Spin.JSON(response.getResponse());
    return RegistryConnectorResponse.builder()
        .responseBody(spin)
        .statusCode(Objects.isNull(spin) ? 404 : 200)
        .build();
  }
}
