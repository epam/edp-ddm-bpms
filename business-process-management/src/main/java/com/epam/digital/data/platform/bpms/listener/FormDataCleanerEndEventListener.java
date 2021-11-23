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

package com.epam.digital.data.platform.bpms.listener;

import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove start form and user task form data from ceph before the completion of the business process
 * instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormDataCleanerEndEventListener implements ExecutionListener {

  private static final String USER_TASK_FORM_DATA_PREFIX_FORMAT = "process/%s/";

  private final S3ObjectCephService s3FormDataStorageCephService;
  private final StartFormCephKeyVariable startFormCephKeyVariable;

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    var startFormDataCephKey = startFormCephKeyVariable.from(execution).get();
    var processInstanceId = execution.getProcessInstanceId();
    var userFormDataPrefixKey = String.format(USER_TASK_FORM_DATA_PREFIX_FORMAT, processInstanceId);

    try {
      var userFormDataCephKeys = s3FormDataStorageCephService.getKeys(userFormDataPrefixKey);
      if (Objects.nonNull(startFormDataCephKey)) {
        userFormDataCephKeys.add(startFormDataCephKey);
      }
      if (!userFormDataCephKeys.isEmpty()) {
        s3FormDataStorageCephService.delete(userFormDataCephKeys);
      }
    } catch (RuntimeException ex) {
      log.warn(
          "Error while deleting form data from ceph, processDefinitionId={}, processInstanceId={}, userFormDataPrefix={}, startFormDataCephKey={}",
          execution.getProcessDefinitionId(), processInstanceId, userFormDataPrefixKey,
          startFormDataCephKey, ex);
    }
  }
}
