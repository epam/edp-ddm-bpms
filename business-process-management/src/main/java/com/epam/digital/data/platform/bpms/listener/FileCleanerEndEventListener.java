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

import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove files from ceph before the completion of the business process instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanerEndEventListener implements ExecutionListener {

  private static final String PREFIX_FORMAT = "process/%s/";

  private final S3ObjectCephService s3FileStorageCephService;

  @Override
  public void notify(DelegateExecution execution) {
    var processInstanceId = execution.getProcessInstanceId();
    var prefix = String.format(PREFIX_FORMAT, processInstanceId);
    try {
      var keys = s3FileStorageCephService.getKeys(prefix);
      if (!keys.isEmpty()) {
        s3FileStorageCephService.delete(keys);
      }
      log.debug("Deleted next files from ceph - {}. ProcessDefinitionId={}, processInstanceId={}",
          keys, execution.getProcessDefinitionId(), processInstanceId);
    } catch (RuntimeException ex) {
      log.warn(
          "Error while deleting documents, processDefinitionId={}, processInstanceId={}, files prefix={}",
          execution.getProcessDefinitionId(), processInstanceId, prefix, ex);
    }
  }
}
