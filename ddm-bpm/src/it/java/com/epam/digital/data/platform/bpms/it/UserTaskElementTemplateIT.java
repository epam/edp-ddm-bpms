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

package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class UserTaskElementTemplateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testUserTemplateEmptyAssignee.bpmn"})
  public void testUserTaskElementTemplateEmptyAssignee() throws Exception {
    var processInstance = runtimeService.startProcessInstanceByKey("testEmptyAssignee_key", "");

    var taskForUserWithoutAccess = getForTasks("/json/testuser2AccessToken.json",
        processInstance.getId());
    var taskForUserWithAccess = getForTasks("/json/testuserAccessToken.json",
        processInstance.getId());
    var task = taskForUserWithAccess[0];
    var identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());

    assertThat(taskForUserWithoutAccess).isEmpty();
    assertThat(taskForUserWithAccess).hasSize(1);
    assertThat(identityLinksForTask.get(0).getUserId()).isEqualTo("testuser");
    assertThat(task.getAssignee()).isNull();
  }

  private TaskDto[] getForTasks(String tokenFilePath, String id) throws IOException {
    String testUserToken = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream(tokenFilePath)));
    return getForObject(String.format("api/task?processInstanceId=%s", id), TaskDto[].class,
        testUserToken);
  }
}
