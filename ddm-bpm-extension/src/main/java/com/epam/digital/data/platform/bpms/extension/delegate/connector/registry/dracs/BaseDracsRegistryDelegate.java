package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry.dracs;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.dto.DracsCertificateListDto;
import com.epam.digital.data.platform.starter.trembita.integration.dracs.service.DracsRemoteService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.spin.Spin;

/**
 * The base java delegate class for Dracs registry connectors. It contains common logic.
 */
@RequiredArgsConstructor
public abstract class BaseDracsRegistryDelegate extends BaseJavaDelegate {

  protected final DracsRemoteService dracsRemoteService;

  @SystemVariable(name = "certNumber")
  protected NamedVariableAccessor<String> certNumberVariable;
  @SystemVariable(name = "certSerial")
  protected NamedVariableAccessor<String> certSerialVariable;
  @SystemVariable(name = "role")
  protected NamedVariableAccessor<String> roleVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<RegistryConnectorResponse> responseVariable;

  protected RegistryConnectorResponse prepareResponse(
      DracsCertificateListDto dracsCertificateListDto) {
    var spin = Objects.isNull(dracsCertificateListDto) ? null : Spin.JSON(dracsCertificateListDto);
    return RegistryConnectorResponse.builder()
        .responseBody(spin)
        .statusCode(Objects.isNull(spin) ? 404 : 200)
        .build();
  }
}
