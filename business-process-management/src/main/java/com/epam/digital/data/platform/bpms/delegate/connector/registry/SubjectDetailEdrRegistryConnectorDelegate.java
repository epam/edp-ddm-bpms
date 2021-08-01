package com.epam.digital.data.platform.bpms.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectDetailDataDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.math.BigInteger;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subject
 * details in EDR registry.
 */
@Slf4j
@RequiredArgsConstructor
public class SubjectDetailEdrRegistryConnectorDelegate implements JavaDelegate {

  private static final String ID_VARIABLE = "id";
  private static final String RESPONSE_VARIABLE = "response";
  private static final String AUTHORIZATION_TOKEN_VARIABLE = "authorizationToken";

  private final EdrRemoteService edrRemoteService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var authorizationToken = (String) execution.getVariable(AUTHORIZATION_TOKEN_VARIABLE);
    var id = (String) execution.getVariable(ID_VARIABLE);

    var response = edrRemoteService.getSubjectDetail(new BigInteger(id), authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);
    log.debug("Edr Registry Subject Detail response: {}", connectorResponse);

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient(RESPONSE_VARIABLE, connectorResponse);
  }

  private EdrRegistryConnectorResponse prepareConnectorResponse(SubjectDetailDataDto response) {
    var spin = Objects.isNull(response) ? null : Spin.JSON(response);
    return EdrRegistryConnectorResponse.builder()
        .responseBody(spin)
        .statusCode(Objects.isNull(spin) ? 404 : 200)
        .build();
  }
}
