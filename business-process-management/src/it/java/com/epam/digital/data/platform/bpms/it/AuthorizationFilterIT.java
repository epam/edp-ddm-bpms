package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.stream.Stream;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Before;
import org.junit.Test;

public class AuthorizationFilterIT extends BaseIT {

  @Before
  public void init() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Test
  public void shouldReadProcessInstanceHistory() throws Exception {
    //get process-definition
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    //create process instance
    ProcessInstanceDto createdProcessInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    //get process-instance history
    HistoricProcessInstanceDto[] historyProcessInstanceDtos = getForObject(
        "api/history/process-instance", HistoricProcessInstanceDto[].class);

    assertThat(historyProcessInstanceDtos).isNotEmpty();
    Stream.of(historyProcessInstanceDtos).forEach(historyProcessInstance -> {
      assertThat(historyProcessInstance.getStartUserId()).isEqualTo("testuser");
    });
  }

  @Test
  public void shouldNotReadProcessInstanceHistory() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    //create process instance
    ProcessInstanceDto createdProcessInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    //get process-instance history by another user
    String testuser2Token = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/json/testuser2AccessToken.json")));
    HistoricProcessInstanceDto[] historyProcessInstanceDtos = getForObject(
        "api/history/process-instance", HistoricProcessInstanceDto[].class, testuser2Token);

    assertThat(historyProcessInstanceDtos).isEmpty();
  }

  @Test
  public void shouldReadUserTasksHistory() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    ProcessInstanceDto processInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    HistoricTaskInstanceEntity[] historyProcessInstanceDtos = getForObject(
        String.format("api/history/task?processInstanceId=%s", processInstance.getId()),
        HistoricTaskInstanceEntity[].class);

    assertThat(historyProcessInstanceDtos).isNotEmpty();
    Stream.of(historyProcessInstanceDtos).forEach(historyTask -> {
      assertThat(historyTask.getAssignee()).isEqualTo("testuser");
    });
  }

  @Test
  public void shouldNotReadUserTasksHistory() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();
    //create process instance
    ProcessInstanceDto processInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    //get user tasks by another user
    String testuser2Token = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/json/testuser2AccessToken.json")));
    HistoricTaskInstanceEntity[] historyProcessInstanceDtos = getForObject(
        String.format("api/history/task?processInstanceId=%s", processInstance.getId()),
        HistoricTaskInstanceEntity[].class, testuser2Token);

    assertThat(historyProcessInstanceDtos).isEmpty();
  }

  @Test
  public void shouldReadUserTasks() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    ProcessInstanceDto processInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    TaskDto[] tasks = getForObject(
        String.format("api/task?processInstanceId=%s", processInstance.getId()), TaskDto[].class);

    assertThat(tasks).isNotEmpty();
    Stream.of(tasks).forEach(task -> assertThat(task.getAssignee()).isEqualTo("testuser"));
  }

  @Test
  public void shouldNotReadUserTasks() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();
    //create process instance
    postForObject("api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    //get user tasks by another user
    String testuser2Token = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/json/testuser2AccessToken.json")));
    TaskDto[] historyProcessInstanceDtos = getForObject(
        "api/task", TaskDto[].class, testuser2Token);

    assertThat(historyProcessInstanceDtos).isEmpty();
  }

  @Test
  public void shouldReadOnlyPermittedTasks() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    //testuser
    ProcessInstanceDto testUserProcessInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    //testuser2
    String testuser2Token = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/json/testuser2AccessToken.json")));
    ProcessInstanceDto testUser2ProcessInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    TaskDto[] testuserTasks = getForObject(
        String.format("api/task?processInstanceId=%s", testUserProcessInstance.getId()),
        TaskDto[].class);
    assertThat(testuserTasks).isNotEmpty();
    Stream.of(testuserTasks).forEach(historyTask -> {
      assertThat(historyTask.getAssignee()).isEqualTo("testuser");
    });

    TaskDto[] testuser2Tasks = getForObject(
        String.format("api/task?processInstanceId=%s", testUser2ProcessInstance.getId()),
        TaskDto[].class, testuser2Token);
    assertThat(testuserTasks).isNotEmpty();
    Stream.of(testuser2Tasks).forEach(historyTask -> {
      assertThat(historyTask.getAssignee()).isEqualTo("testuser2");
    });
  }

  @Test
  public void shouldFilterTasksWithoutIdentityLinks() throws IOException {
    ProcessDefinitionDto[] processDefinitionDtos = getForObject("api/process-definition",
        ProcessDefinitionDto[].class);
    ProcessDefinitionDto processDefinition = Stream.of(processDefinitionDtos)
        .filter(pd -> "testInitSystemVariablesProcess_key".equals(pd.getKey())).findFirst().get();

    ProcessInstanceDto processInstance = postForObject(
        "api/process-definition/" + processDefinition.getId() + "/start", "{}",
        ProcessInstanceDto.class);

    TaskDto[] tasks = getForObject(
        String.format("api/task?processInstanceId=%s", processInstance.getId()), TaskDto[].class);

    String changeAssigneeUtl = "api/task/" + tasks[0].getId() + "/assignee";
    jerseyClient.target(String.format("http://localhost:%d/%s", port, changeAssigneeUtl))
        .request()
        .header(TOKEN_HEADER, validAccessToken)
        .post(Entity.entity("{\"userId\":null}", MediaType.APPLICATION_JSON));

    TaskDto[] tasks2 = getForObject(
        String.format("api/task?processInstanceId=%s", processInstance.getId()), TaskDto[].class);

    assertThat(tasks2).isEmpty();
  }
}
