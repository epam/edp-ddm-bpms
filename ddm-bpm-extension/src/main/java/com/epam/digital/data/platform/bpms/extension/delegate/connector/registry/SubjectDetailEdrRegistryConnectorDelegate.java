package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectDetailDataDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subject
 * details in EDR registry.
 */
@RequiredArgsConstructor
public class SubjectDetailEdrRegistryConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "subjectDetailEdrRegistryConnectorDelegate";

  private static final String ID_VARIABLE = "id";
  private static final String RESPONSE_VARIABLE = "response";
  private static final String AUTHORIZATION_TOKEN_VARIABLE = "authorizationToken";

  private final EdrRemoteService edrRemoteService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var authorizationToken = (String) execution.getVariable(AUTHORIZATION_TOKEN_VARIABLE);
    var id = (String) execution.getVariable(ID_VARIABLE);

    logProcessExecution("get subject detail by id", id);
    var response = edrRemoteService.getSubjectDetail(new BigInteger(id), authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);

    setTransientResult(execution, RESPONSE_VARIABLE, connectorResponse);
    logDelegateExecution(execution, Set.of(AUTHORIZATION_TOKEN_VARIABLE, ID_VARIABLE),
        Set.of(RESPONSE_VARIABLE));
  }

  private EdrRegistryConnectorResponse prepareConnectorResponse(SubjectDetailDataDto response) {
    var spin = Objects.isNull(response) ? null : Spin.JSON(response);
    return EdrRegistryConnectorResponse.builder()
        .responseBody(spin)
        .statusCode(Objects.isNull(spin) ? 404 : 200)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
