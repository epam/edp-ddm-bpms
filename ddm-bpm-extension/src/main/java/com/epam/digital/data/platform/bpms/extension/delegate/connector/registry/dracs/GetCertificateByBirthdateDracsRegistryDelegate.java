package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.DracsGetByBirthDateRequestDto;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.Role;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

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
