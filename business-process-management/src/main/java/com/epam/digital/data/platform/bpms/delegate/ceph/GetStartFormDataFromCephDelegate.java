package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import java.util.LinkedHashMap;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from ceph using {@link FormDataCephService}
 * service for start form, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return
 * it.
 */
@Component(GetStartFormDataFromCephDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class GetStartFormDataFromCephDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "getStartFormDataFromCephDelegate";
  private static final String FORM_DATA_PARAMETER = "formData";

  private final FormDataCephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var cephKey = (String) execution.getVariable(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME);

    logProcessExecution("get start form data by key", cephKey);
    var formData = cephService.getFormData(cephKey)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());

    setTransientResult(execution, FORM_DATA_PARAMETER, Spin.JSON(formData));
    logDelegateExecution(execution, Set.of(Constants.BPMS_START_FORM_CEPH_KEY_VARIABLE_NAME),
        Set.of(FORM_DATA_PARAMETER));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
