package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.EdrRegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dto.SubjectDetailDataDto;
import com.epam.digital.data.platform.starter.trembita.integration.service.EdrRemoteService;
import java.math.BigInteger;
import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subject
 * details in EDR registry.
 */
public class SubjectDetailEdrRegistryConnectorDelegate extends BaseEdrRegistryConnectorDelegate {

  public static final String DELEGATE_NAME = "subjectDetailEdrRegistryConnectorDelegate";

  @SystemVariable(name = "id")
  private NamedVariableAccessor<String> idVariable;

  public SubjectDetailEdrRegistryConnectorDelegate(EdrRemoteService edrRemoteService) {
    super(edrRemoteService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var authorizationToken = authorizationTokenVariable.from(execution).get();
    var id = idVariable.from(execution).get();
    Objects.requireNonNull(id,
        "'id' parameter is null in subjectDetailEdrRegistryConnectorDelegate");

    logProcessExecution("get subject detail by id", id);
    var response = edrRemoteService.getSubjectDetail(new BigInteger(id), authorizationToken);
    var connectorResponse = prepareConnectorResponse(response);

    responseVariable.on(execution).set(connectorResponse);
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
