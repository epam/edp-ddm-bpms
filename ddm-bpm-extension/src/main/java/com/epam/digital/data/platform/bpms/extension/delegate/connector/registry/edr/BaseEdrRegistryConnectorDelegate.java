package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.edr;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.edr.service.EdrRemoteService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to search subjects in
 * EDR registry.
 */
@RequiredArgsConstructor
public abstract class BaseEdrRegistryConnectorDelegate extends BaseJavaDelegate {

  protected final EdrRemoteService edrRemoteService;

  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<RegistryConnectorResponse> responseVariable;
}

