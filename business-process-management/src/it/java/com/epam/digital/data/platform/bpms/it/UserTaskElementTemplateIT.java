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
    var identityLinksForTask = engine.getTaskService()
        .getIdentityLinksForTask(task.getId());

    assertThat(taskForUserWithoutAccess.length).isEqualTo(0);
    assertThat(taskForUserWithAccess.length).isEqualTo(1);
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
