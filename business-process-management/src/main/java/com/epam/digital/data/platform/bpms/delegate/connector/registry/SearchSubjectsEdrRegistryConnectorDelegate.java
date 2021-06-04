package com.epam.digital.data.platform.bpms.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.client.EdrRegistryClient;
import com.epam.digital.data.platform.bpms.client.dto.SearchSubjectRequestDto;
import com.epam.digital.data.platform.bpms.client.dto.SubjectListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
@Slf4j
@RequiredArgsConstructor
public class SearchSubjectsEdrRegistryConnectorDelegate implements JavaDelegate {

  protected static final String EDR_CODE_VARIABLE = "code";
  protected static final String EDR_NAME_VARIABLE = "name";
  protected static final String EDR_PASSPORT_VARIABLE = "passport";
  protected static final String RESPONSE_VARIABLE = "response";
  protected static final String AUTHORIZATION_TOKEN_VARIABLE = "authorizationToken";

  private final EdrRegistryClient client;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var authorizationToken = (String) execution.getVariable(AUTHORIZATION_TOKEN_VARIABLE);

    SearchSubjectRequestDto searchSubjectRequest = createSearchSubjectRequest(execution);
    log.debug("Searching subjects in EDR registry, request: {}", searchSubjectRequest);
    SubjectListDto response = client.searchSubjects(searchSubjectRequest, authorizationToken);
    log.debug("Searching subjects in EDR registry, response: {}", response);

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient(RESPONSE_VARIABLE, Spin.JSON(response));
  }

  private SearchSubjectRequestDto createSearchSubjectRequest(DelegateExecution execution) {
    return SearchSubjectRequestDto.builder()
        .passport((String) execution.getVariable(EDR_PASSPORT_VARIABLE))
        .code((String) execution.getVariable(EDR_CODE_VARIABLE))
        .name((String) execution.getVariable(EDR_NAME_VARIABLE))
        .build();
  }
}

