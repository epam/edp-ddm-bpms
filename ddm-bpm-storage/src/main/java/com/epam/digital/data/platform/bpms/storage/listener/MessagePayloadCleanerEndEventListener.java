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

import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove start form and user task form data from storage before the completion of the business
 * process instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePayloadCleanerEndEventListener implements ExecutionListener {

  private final MessagePayloadStorageService messagePayloadStorageService;
  private final StartMessagePayloadStorageKeyVariable startMessagePayloadStorageKeyVariable;

  @Override
  public void notify(DelegateExecution execution) {
    var startMessagePayloadStorageKey = startMessagePayloadStorageKeyVariable.from(execution).get();
    var processInstanceId = execution.getProcessInstanceId();
    try {
      deleteMessagePayload(startMessagePayloadStorageKey);
    } catch (RuntimeException ex) {
      log.warn(
          "Error while deleting message payload from ceph, processDefinitionId={}, processInstanceId={}, startFormDataCephKey={}",
          execution.getProcessDefinitionId(), processInstanceId, startMessagePayloadStorageKey, ex);
    }
  }

  private void deleteMessagePayload(String startMessagePayloadStorageKey) {
    if (Objects.nonNull(startMessagePayloadStorageKey)) {
      messagePayloadStorageService.deleteMessagePayload(startMessagePayloadStorageKey);
    }
  }
}
