package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.DracsGetByNameRequestDto;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.Role;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * The java delegate that allows getting certificate from Dracs registry by partial id and
 * fullname.
 */
@Slf4j
public class GetCertificateByNameDracsRegistryDelegate extends BaseDracsRegistryDelegate {

  public static final String DELEGATE_NAME = "getCertificateByNameDracsRegistryDelegate";

  @SystemVariable(name = "name")
  private NamedVariableAccessor<String> nameVariable;
  @SystemVariable(name = "surname")
  private NamedVariableAccessor<String> surnameVariable;
  @SystemVariable(name = "patronymic")
  private NamedVariableAccessor<String> patronymicVariable;

  public GetCertificateByNameDracsRegistryDelegate(DracsRemoteService dracsRemoteService) {
    super(dracsRemoteService);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var request = createRequest(execution);
    log.debug("Start searching certificate by name, request {}", request);
    var result = dracsRemoteService.getGetCertByNumRoleNames(request);
    var response = prepareResponse(result);
    log.debug("Get response with code {}", response.getStatusCode());

    responseVariable.on(execution).set(response);
  }

  private DracsGetByNameRequestDto createRequest(DelegateExecution execution) {
    return DracsGetByNameRequestDto.builder()
        .role(Role.getByValue(roleVariable.from(execution).getOrThrow()))
        .certNumber(certNumberVariable.from(execution).getOrThrow())
        .certSerial(certSerialVariable.from(execution).getOrThrow())
        .patronymic(patronymicVariable.from(execution).getOrThrow())
        .surname(surnameVariable.from(execution).getOrThrow())
        .name(nameVariable.from(execution).getOrThrow())
        .build();
  }
}
