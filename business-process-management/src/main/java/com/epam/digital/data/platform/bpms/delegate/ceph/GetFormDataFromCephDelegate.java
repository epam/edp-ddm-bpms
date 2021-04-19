package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from ceph using {@link FormDataCephService}
 * service, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return it.
 */
@Component("getFormDataFromCephDelegate")
@Logging
@RequiredArgsConstructor
public class GetFormDataFromCephDelegate implements JavaDelegate {

  private final FormDataCephService cephService;
  private final CephKeyProvider cephKeyProvider;

  @Override
  public void execute(DelegateExecution execution) {
    var taskDefinitionKey = (String) execution.getVariable("taskDefinitionKey");

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, execution.getProcessInstanceId());
    var formData = cephService.getFormData(cephKey);

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient("formData", Spin.JSON(formData.getData()));
  }
}
