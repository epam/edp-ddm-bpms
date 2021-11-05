package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class used to map {@link SpinJsonNode} to {@link FormDataDto} entity and put in ceph using
 * {@link FormDataCephService} service.
 */
@Slf4j
@Component(PutFormDataToCephDelegate.DELEGATE_NAME)
public class PutFormDataToCephDelegate extends BaseFormDataDelegate {

  public static final String DELEGATE_NAME = "putFormDataToCephDelegate";

  private static final LinkedHashMapTypeReference FORM_DATA_TYPE = new LinkedHashMapTypeReference();

  private final ObjectMapper objectMapper;

  public PutFormDataToCephDelegate(FormDataCephService cephService, CephKeyProvider cephKeyProvider,
      ObjectMapper objectMapper) {
    super(cephService, cephKeyProvider);
    this.objectMapper = objectMapper;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var taskDefinitionKey = taskDefinitionKeyVariable.from(execution).get();
    var processInstanceId = execution.getProcessInstanceId();

    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);
    var formData = formDataVariable.from(execution).getOrDefault(Spin.JSON(Map.of()));

    log.debug("Start putting form data with key {}", cephKey);
    cephService.putFormData(cephKey, toFormDataDto(formData));
    log.debug("Form data put successfully with key {}", cephKey);
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
