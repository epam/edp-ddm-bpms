package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
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
 * service, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return it.
 */
@Component(GetFormDataFromCephDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class GetFormDataFromCephDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "getFormDataFromCephDelegate";
  private static final String TASK_DEFINITION_KEY_PARAMETER = "taskDefinitionKey";
  private static final String FORM_DATA_PARAMETER = "formData";

  private final FormDataCephService cephService;
  private final CephKeyProvider cephKeyProvider;

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var taskDefinitionKey = (String) execution.getVariable(TASK_DEFINITION_KEY_PARAMETER);

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, execution.getProcessInstanceId());

    logProcessExecution("get form data by key", cephKey);
    var formData = cephService.getFormData(cephKey)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());

    setTransientResult(execution, FORM_DATA_PARAMETER, Spin.JSON(formData));
    logDelegateExecution(execution, Set.of(TASK_DEFINITION_KEY_PARAMETER),
        Set.of(FORM_DATA_PARAMETER));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
