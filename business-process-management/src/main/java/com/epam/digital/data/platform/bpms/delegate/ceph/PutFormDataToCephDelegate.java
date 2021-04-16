package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.bpms.delegate.constants.CamundaDelegateConstants;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class used to map {@link SpinJsonNode} to {@link FormDataDto} entity and put in ceph using
 * {@link FormDataCephService} service.
 */
@Component("putFormDataToCephDelegate")
@Logging
@RequiredArgsConstructor
public class PutFormDataToCephDelegate implements JavaDelegate {

  private static final LinkedHashMapTypeReference FORM_DATA_TYPE = new LinkedHashMapTypeReference();

  private final FormDataCephService cephService;
  private final ObjectMapper objectMapper;

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var taskDefinitionKey = (String) delegateExecution.getVariable("taskDefinitionKey");
    var taskFormDataVariableName = String.format(
        CamundaDelegateConstants.TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);

    var processInstanceId = delegateExecution.getProcessInstanceId();
    var cephKey = String.format(CamundaDelegateConstants.TASK_FORM_DATA_VALUE_FORMAT,
        processInstanceId, taskFormDataVariableName);
    var formData = (SpinJsonNode) delegateExecution.getVariable("formData");

    cephService.putFormData(cephKey, toFormDataDto(formData));
    delegateExecution.setVariable(taskFormDataVariableName, cephKey);
  }

  /**
   * Convert SpinJsonNode data to {@link FormDataDto} entity with empty signature and accessToken
   *
   * @param formData SpinJsonNode for converting
   * @return {@link FormDataDto} entity
   */
  private FormDataDto toFormDataDto(SpinJsonNode formData) {
    var data = objectMapper.convertValue(formData.unwrap(), FORM_DATA_TYPE);
    return FormDataDto.builder().data(data).build();
  }

  private static class LinkedHashMapTypeReference extends
      TypeReference<LinkedHashMap<String, Object>> {

  }
}
