package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectInfoDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.util.CollectionUtils;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
@RequiredArgsConstructor
public class SearchSubjectsEdrRegistryConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "searchSubjectsEdrRegistryConnectorDelegate";

  protected static final String EDR_CODE_VARIABLE = "code";
  protected static final String RESPONSE_VARIABLE = "response";
  protected static final String AUTHORIZATION_TOKEN_VARIABLE = "authorizationToken";

  private final EdrRemoteService edrRemoteService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var code = (String) execution.getVariable(EDR_CODE_VARIABLE);
    var authorizationToken = (String) execution.getVariable(AUTHORIZATION_TOKEN_VARIABLE);

    logProcessExecution("search subjects by code", code);
    var response = edrRemoteService.searchSubjects(code, authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);

    setTransientResult(execution, RESPONSE_VARIABLE, connectorResponse);
    logDelegateExecution(execution, Set.of(EDR_CODE_VARIABLE, AUTHORIZATION_TOKEN_VARIABLE),
        Set.of(RESPONSE_VARIABLE));
  }

  private EdrRegistryConnectorResponse prepareConnectorResponse(List<SubjectInfoDto> response) {
    return EdrRegistryConnectorResponse.builder()
        .responseBody(Spin.JSON(response))
        .statusCode(CollectionUtils.isEmpty(response) ? 404 : 200)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}

