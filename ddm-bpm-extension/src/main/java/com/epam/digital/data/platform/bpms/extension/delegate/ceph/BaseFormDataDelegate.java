package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import lombok.RequiredArgsConstructor;
import org.camunda.spin.json.SpinJsonNode;

/**
 * The class used to access {@link FormDataDto} entity in ceph using {@link FormDataCephService}
 */
@RequiredArgsConstructor
public abstract class BaseFormDataDelegate extends BaseJavaDelegate {

  protected final FormDataCephService cephService;
  protected final CephKeyProvider cephKeyProvider;

  @SystemVariable(name = "taskDefinitionKey")
  protected NamedVariableAccessor<String> taskDefinitionKeyVariable;
  @SystemVariable(name = "formData", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> formDataVariable;
}
