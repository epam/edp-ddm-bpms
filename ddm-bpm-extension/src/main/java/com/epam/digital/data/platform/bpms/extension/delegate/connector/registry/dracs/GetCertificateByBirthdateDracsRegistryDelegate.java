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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.DracsGetByBirthDateRequestDto;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.Role;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * The java delegate that allows getting certificate from Dracs registry by partial id and
 * birthdate.
 */
@Slf4j
public class GetCertificateByBirthdateDracsRegistryDelegate extends BaseDracsRegistryDelegate {

  public static final String DELEGATE_NAME = "getCertificateByBirthdateDracsRegistryDelegate";

  @SystemVariable(name = "birthYear")
  private NamedVariableAccessor<String> birthYearVariable;
  @SystemVariable(name = "birthMonth")
  private NamedVariableAccessor<String> birthMonthVariable;
  @SystemVariable(name = "birthDay")
  private NamedVariableAccessor<String> birthDayVariable;

  public GetCertificateByBirthdateDracsRegistryDelegate(DracsRemoteService dracsRemoteService) {
    super(dracsRemoteService);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    responseVariable.on(execution).set(RegistryConnectorResponse.builder().build());

    var request = createRequest(execution);
    log.debug("Start searching certificate by birthdate, request {}", request);
    var result = dracsRemoteService.getCertByNumRoleBirthDate(request);
    var response = prepareResponse(result);
    log.debug("Get response with code {}", response.getStatusCode());

    responseVariable.on(execution).set(response);
  }

  private DracsGetByBirthDateRequestDto createRequest(DelegateExecution execution) {
    return DracsGetByBirthDateRequestDto.builder()
        .role(Role.getByValue(roleVariable.from(execution).getOrThrow()))
        .certNumber(certNumberVariable.from(execution).getOrThrow())
        .certSerial(certSerialVariable.from(execution).getOrThrow())
        .birthdate(getBirthdate(execution))
        .build();
  }

  private LocalDate getBirthdate(DelegateExecution execution) {
    var year = Integer.parseInt(birthYearVariable.from(execution).getOrThrow());
    var mouth = Integer.parseInt(birthMonthVariable.from(execution).getOrThrow());
    var day = Integer.parseInt(birthDayVariable.from(execution).getOrThrow());
    return LocalDate.of(year, mouth, day);
  }

}
