package com.epam.digital.data.platform.bpms.listener;

import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link TaskListener} listener that is used to save
 * {@code userTaskInputFormDataPrepopulate} user task input parameter to ceph.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutFormDataToCephTaskListener implements TaskListener {

  private static final String FORM_DATA_INPUT_PARAMETER = "userTaskInputFormDataPrepopulate";

  private static final PutFormDataToCephTaskListener.LinkedHashMapTypeReference FORM_DATA_TYPE =
      new PutFormDataToCephTaskListener.LinkedHashMapTypeReference();

  private final FormDataCephService formDataCephService;
  private final CephKeyProvider cephKeyProvider;
  private final ObjectMapper objectMapper;

  @Override
  public void notify(DelegateTask delegateTask) {
    var taskDefinitionKey = delegateTask.getTaskDefinitionKey();
    var processInstanceId = delegateTask.getProcessInstanceId();

    var variableLocal = delegateTask.getExecution().getVariableLocal(FORM_DATA_INPUT_PARAMETER);
    if (Objects.isNull(variableLocal)) {
      return;
    } else if (!(variableLocal instanceof SpinJsonNode)) {
      log.warn(
          "Form data pre-population for task {} in process {} has wrong type. {} instead of {}",
          taskDefinitionKey, processInstanceId, variableLocal.getClass(), SpinJsonNode.class);
      return;
    }

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    var formData = (SpinJsonNode) variableLocal;
    var data = objectMapper.convertValue(formData.unwrap(), FORM_DATA_TYPE);
    var formDataDto = FormDataDto.builder().data(data).build();
    log.debug("Putting form-data to ceph.\n"
            + "Task-definition-key - {}\n"
            + "Ceph-key - {}\n"
            + "Form-data - {}\n"
            + "Process-definition-id - {}\n"
            + "Process-instance-id - {}",
        taskDefinitionKey, cephKey, formData, delegateTask.getProcessDefinitionId(),
        processInstanceId);
    formDataCephService.putFormData(cephKey, formDataDto);
  }

  private static class LinkedHashMapTypeReference extends
      TypeReference<LinkedHashMap<String, Object>> {

  }
}
