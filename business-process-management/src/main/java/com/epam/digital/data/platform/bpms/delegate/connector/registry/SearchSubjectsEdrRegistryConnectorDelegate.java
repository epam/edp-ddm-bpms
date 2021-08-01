package com.epam.digital.data.platform.bpms.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectInfoDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.springframework.util.CollectionUtils;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
@Slf4j
@RequiredArgsConstructor
public class SearchSubjectsEdrRegistryConnectorDelegate implements JavaDelegate {

  protected static final String EDR_CODE_VARIABLE = "code";
  protected static final String RESPONSE_VARIABLE = "response";
  protected static final String AUTHORIZATION_TOKEN_VARIABLE = "authorizationToken";

  private final EdrRemoteService edrRemoteService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var code = (String) execution.getVariable(EDR_CODE_VARIABLE);
    var authorizationToken = (String) execution.getVariable(AUTHORIZATION_TOKEN_VARIABLE);

    var response = edrRemoteService.searchSubjects(code, authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);
    log.debug("Edr Registry Search Subjects response: {}", connectorResponse);

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient(RESPONSE_VARIABLE, connectorResponse);
  }

  private EdrRegistryConnectorResponse prepareConnectorResponse(List<SubjectInfoDto> response) {
    return EdrRegistryConnectorResponse.builder()
        .responseBody(Spin.JSON(response))
        .statusCode(CollectionUtils.isEmpty(response) ? 404 : 200)
        .build();
  }
}

