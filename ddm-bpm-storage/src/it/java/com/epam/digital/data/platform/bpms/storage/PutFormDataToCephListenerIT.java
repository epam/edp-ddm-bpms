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

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.Test;

class PutFormDataToCephListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/testPutFormDataListener.bpmn")
  void testPutFormDataToCephListener() {
    var taskDefKey = "user_task";
    var processInstance = runtimeService.startProcessInstanceByKey("testPutFormDataListener");

    BpmnAwareTests.assertThat(processInstance).isWaitingAt(taskDefKey);
    var formData = formDataStorageService.getFormData(taskDefKey, processInstance.getId());
    assertThat(formData.get().getData()).hasSize(2).containsEntry("field1", "value1")
        .containsEntry("field2", "value2");
  }
}
