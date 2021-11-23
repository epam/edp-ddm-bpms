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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.edr.dto.SubjectDetailDataDto;
import com.epam.digital.data.platform.starter.trembita.integration.edr.service.EdrRemoteService;
import java.math.BigInteger;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subject
 * details in EDR registry.
 */
@Slf4j
public class SubjectDetailEdrRegistryConnectorDelegate extends BaseEdrRegistryConnectorDelegate {

  public static final String DELEGATE_NAME = "subjectDetailEdrRegistryConnectorDelegate";

  @SystemVariable(name = "id")
  private NamedVariableAccessor<String> idVariable;

  public SubjectDetailEdrRegistryConnectorDelegate(EdrRemoteService edrRemoteService) {
    super(edrRemoteService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var id = idVariable.from(execution).get();
    Objects.requireNonNull(id,
        "'id' parameter is null in subjectDetailEdrRegistryConnectorDelegate");

    log.debug("Start getting subject detail by id {}", id);
    var response = edrRemoteService.getSubjectDetail(new BigInteger(id));
    var connectorResponse = prepareConnectorResponse(response);
    log.debug("Got subject detail response with status: {}", connectorResponse.getStatusCode());

    responseVariable.on(execution).set(connectorResponse);
  }

  private RegistryConnectorResponse prepareConnectorResponse(SubjectDetailDataDto response) {
    var spin = Objects.isNull(response) ? null : Spin.JSON(response);
    return RegistryConnectorResponse.builder()
        .responseBody(spin)
        .statusCode(Objects.isNull(spin) ? 404 : 200)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
