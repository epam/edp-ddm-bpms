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

package com.epam.digital.data.platform.bpms.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.storage.file.dto.FileDataDto;
import com.epam.digital.data.platform.storage.file.dto.FileMetadataDto;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class FileCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/file_cleaner_listener.bpmn")
  void testFileCleanerEndEventListener() {
    var data = new ByteArrayInputStream(new byte[]{1});
    var fileDataDto = FileDataDto.builder()
        .metadata(FileMetadataDto.builder().build())
        .content(data)
        .build();

    var processInstance = runtimeService.startProcessInstanceByKey("fileCleanerListenerKey");
    var processInstanceId = processInstance.getId();
    var taskId = taskService.createTaskQuery().taskDefinitionKey("fileCleanerListenerId")
        .singleResult().getId();
    formDataFileStorageService.save(processInstanceId, UUID.randomUUID().toString(), fileDataDto);
    formDataFileStorageService.save(processInstanceId, UUID.randomUUID().toString(), fileDataDto);

    assertThat(cephService.getStorage()).hasSize(2);

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(cephService.getStorage()).isEmpty();
  }
}
