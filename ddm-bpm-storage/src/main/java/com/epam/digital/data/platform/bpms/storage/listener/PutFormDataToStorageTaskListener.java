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

package com.epam.digital.data.platform.bpms.storage.listener;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
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
public class PutFormDataToStorageTaskListener implements TaskListener {

  private static final PutFormDataToStorageTaskListener.LinkedHashMapTypeReference FORM_DATA_TYPE =
      new PutFormDataToStorageTaskListener.LinkedHashMapTypeReference();

  private final FormDataStorageService formDataStorageService;
  private final ObjectMapper objectMapper;

  @SystemVariable(name = "userTaskInputFormDataPrepopulate", isTransient = true)
  private NamedVariableAccessor<SpinJsonNode> userTaskInputFormDataPrepopulateVariable;

  @Override
  public void notify(DelegateTask delegateTask) {
    var taskDefinitionKey = delegateTask.getTaskDefinitionKey();
    var processInstanceId = delegateTask.getProcessInstanceId();

    var formData = userTaskInputFormDataPrepopulateVariable.from(delegateTask.getExecution()).get();
    if (Objects.isNull(formData)) {
      return;
    }

    var data = objectMapper.convertValue(formData.unwrap(), FORM_DATA_TYPE);
    var formDataDto = FormDataDto.builder().data(data).build();
    log.debug("Putting form-data to ceph.\n"
            + "Task-definition-key - {}\n"
            + "Process-definition-id - {}\n"
            + "Process-instance-id - {}",
        taskDefinitionKey, delegateTask.getProcessDefinitionId(), processInstanceId);
    formDataStorageService.putFormData(taskDefinitionKey, processInstanceId, formDataDto);
  }

  private static class LinkedHashMapTypeReference extends
      TypeReference<LinkedHashMap<String, Object>> {

  }
}
