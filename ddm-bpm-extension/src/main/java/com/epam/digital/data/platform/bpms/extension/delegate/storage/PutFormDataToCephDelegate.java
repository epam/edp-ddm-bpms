/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.extension.delegate.storage;

import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
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
 * The class used to map {@link SpinJsonNode} to {@link FormDataDto} entity and put in storage using
 * {@link FormDataStorageService} service.
 *
 * @deprecated because of form data storage migration from ceph to redis
 */
@Deprecated
@Slf4j
@Component(PutFormDataToCephDelegate.DELEGATE_NAME)
public class PutFormDataToCephDelegate extends BaseFormDataDelegate {

  public static final String DELEGATE_NAME = "putFormDataToCephDelegate";

  private static final LinkedHashMapTypeReference FORM_DATA_TYPE = new LinkedHashMapTypeReference();

  private final ObjectMapper objectMapper;

  public PutFormDataToCephDelegate(FormDataStorageService formDataStorageService,
      ObjectMapper objectMapper) {
    super(formDataStorageService);
    this.objectMapper = objectMapper;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var taskDefinitionKey = taskDefinitionKeyVariable.from(execution).get();
    var processInstanceId = execution.getProcessInstanceId();

    var formData = formDataVariable.from(execution).getOrDefault(Spin.JSON(Map.of()));

    log.debug("Start putting form data with taskDefinitionKey {}, processInstanceId {}",
        taskDefinitionKey, processInstanceId);
    formDataStorageService.putFormData(taskDefinitionKey, processInstanceId,
        toFormDataDto(formData));
    log.debug("Form data put successfully with taskDefinitionKey {}, processInstanceId {}",
        taskDefinitionKey, processInstanceId);
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
