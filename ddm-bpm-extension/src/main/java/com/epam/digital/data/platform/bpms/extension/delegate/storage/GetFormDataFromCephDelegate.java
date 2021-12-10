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
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.stereotype.Component;

/**
 * The class used to get {@link FormDataDto} entity from storage using {@link FormDataStorageService}
 * service, map the formData to {@link org.camunda.spin.json.SpinJsonNode} and return it.
 */
@Slf4j
@Component(GetFormDataFromCephDelegate.DELEGATE_NAME)
public class GetFormDataFromCephDelegate extends BaseFormDataDelegate {

  public static final String DELEGATE_NAME = "getFormDataFromCephDelegate";

  public GetFormDataFromCephDelegate(FormDataStorageService formDataStorageService) {
    super(formDataStorageService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var taskDefinitionKey = taskDefinitionKeyVariable.from(execution).get();
    var processInstanceId = execution.getProcessInstanceId();

    log.debug("Start getting form data by task definition key {}, process instance id {}",
        taskDefinitionKey, processInstanceId);
    var formData = formDataStorageService.getFormData(taskDefinitionKey, processInstanceId)
        .map(FormDataDto::getData)
        .orElse(new LinkedHashMap<>());
    log.debug("Got form data by task definition key {}, process instance id {}", taskDefinitionKey,
        processInstanceId);

    formDataVariable.on(execution).set(Spin.JSON(formData));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
