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
import com.epam.digital.data.platform.starter.trembita.integration.edr.dto.SubjectInfoDto;
import com.epam.digital.data.platform.starter.trembita.integration.edr.service.EdrRemoteService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.util.CollectionUtils;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
@Slf4j
public class SearchSubjectsEdrRegistryConnectorDelegate extends BaseEdrRegistryConnectorDelegate {

  public static final String DELEGATE_NAME = "searchSubjectsEdrRegistryConnectorDelegate";

  @SystemVariable(name = "code")
  private NamedVariableAccessor<String> codeVariable;

  public SearchSubjectsEdrRegistryConnectorDelegate(EdrRemoteService edrRemoteService) {
    super(edrRemoteService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var code = codeVariable.from(execution).get();

    log.debug("Start searching subjects by code {}", code);
    var response = edrRemoteService.searchSubjects(code);
    var connectorResponse = prepareConnectorResponse(response);
    log.debug("Got subjects response with status: {}", connectorResponse.getStatusCode());

    responseVariable.on(execution).set(connectorResponse);
  }

  private RegistryConnectorResponse prepareConnectorResponse(List<SubjectInfoDto> response) {
    return RegistryConnectorResponse.builder()
        .responseBody(Spin.JSON(response))
        .statusCode(CollectionUtils.isEmpty(response) ? 404 : 200)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}

