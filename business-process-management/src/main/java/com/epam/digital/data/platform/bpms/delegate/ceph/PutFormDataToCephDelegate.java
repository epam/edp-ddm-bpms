package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class used to map {@link SpinJsonNode} to {@link FormDataDto} entity and put in ceph using
 * {@link FormDataCephService} service.
 */
@Component(PutFormDataToCephDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class PutFormDataToCephDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "putFormDataToCephDelegate";
  private static final String TASK_DEFINITION_KEY_PARAMETER = "taskDefinitionKey";
  private static final String FORM_DATA_PARAMETER = "formData";

  private static final LinkedHashMapTypeReference FORM_DATA_TYPE = new LinkedHashMapTypeReference();

  private final FormDataCephService cephService;
  private final ObjectMapper objectMapper;
  private final CephKeyProvider cephKeyProvider;

  @Override
  public void execute(DelegateExecution execution) {
    var taskDefinitionKey = (String) execution.getVariable(TASK_DEFINITION_KEY_PARAMETER);
    var processInstanceId = execution.getProcessInstanceId();

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    var formData = (SpinJsonNode) execution.getVariable(FORM_DATA_PARAMETER);

    cephService.putFormData(cephKey, toFormDataDto(formData));
    logDelegateExecution(execution, Set.of(TASK_DEFINITION_KEY_PARAMETER, FORM_DATA_PARAMETER),
        Set.of());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
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
