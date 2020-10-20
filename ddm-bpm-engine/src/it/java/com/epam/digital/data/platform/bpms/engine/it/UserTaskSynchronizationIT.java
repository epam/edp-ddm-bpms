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

package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;

class UserTaskSynchronizationIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testSynchronization.bpmn"})
  void testUserTaskSynchronization() throws InterruptedException {
    var executorService = Executors.newFixedThreadPool(2);

    var processInstance = runtimeService.startProcessInstanceByKey("testSynchronization");

    var taskId = taskService.createTaskQuery()
        .processInstanceId(processInstance.getProcessInstanceId()).singleResult().getId();

    executorService.submit(() -> taskService.complete(taskId));
    Thread.sleep(100);
    var resultFuture = executorService.submit(() -> taskService.complete(taskId));

    var ex = assertThrows(ExecutionException.class, resultFuture::get);

    assertThat(ex.getCause()).isInstanceOf(TaskAlreadyInCompletionException.class);
    assertThat(ex.getCause().getMessage()).isEqualTo("Task " + taskId + " already in completion");
  }
}
