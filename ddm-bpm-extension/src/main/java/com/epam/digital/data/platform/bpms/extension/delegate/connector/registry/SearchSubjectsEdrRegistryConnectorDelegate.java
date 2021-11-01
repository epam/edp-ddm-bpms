package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectInfoDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.util.CollectionUtils;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
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
    var authorizationToken = authorizationTokenVariable.from(execution).get();

    logProcessExecution("search subjects by code", code);
    var response = edrRemoteService.searchSubjects(code, authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);

    responseVariable.on(execution).set(connectorResponse);
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

