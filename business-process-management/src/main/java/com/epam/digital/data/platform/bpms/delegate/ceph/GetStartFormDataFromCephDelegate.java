package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from ceph using {@link FormDataCephService}
 * service for start form, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return
 * it.
 */
@Component("getStartFormDataFromCephDelegate")
@Logging
@RequiredArgsConstructor
public class GetStartFormDataFromCephDelegate implements JavaDelegate {

  private final FormDataCephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    var cephKey = (String) execution.getVariable(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME);
    var formData = cephService.getFormData(cephKey)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient("formData", Spin.JSON(formData));
  }
}
