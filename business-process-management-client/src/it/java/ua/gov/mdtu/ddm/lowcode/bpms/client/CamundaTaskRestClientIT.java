package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDetailsDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.AuthorizationException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.TaskNotFoundException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.UserDataValidationException;

public class CamundaTaskRestClientIT extends BaseIT {

  @Autowired
  private CamundaTaskRestClient camundaTaskRestClient;

  @Before
  public void init() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );
    TaskDto taskDto = new TaskDto();
    taskDto.setAssignee("testAssignee");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto))))
        )
    );
    TaskDto taskDtoById = new TaskDto();
    taskDtoById.setId("tid");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(taskDtoById)))
        )
    );
    ErrorDto errorDto403 = new ErrorDto("type", "message403");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid403"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(403)
                .withBody(objectMapper.writeValueAsString(errorDto403)))
        )
    );
    ErrorDto errorDto404 = new ErrorDto("type", "message404");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid404"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(404)
                .withBody(objectMapper.writeValueAsString(errorDto404)))
        )
    );
    Map<String, VariableValueDto> completeVariables = new HashMap<>();
    completeVariables.put("var1", new VariableValueDto());
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/task/testId/complete"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(completeVariables))))
    );
    //get tasks by processInstanceIdIn
    TaskEntity task = new TaskEntity();
    task.setProcessInstanceId("testProcessInstanceId");
    TaskEntity task2 = new TaskEntity();
    task2.setProcessInstanceId("testProcessInstanceId2");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task"))
            .withQueryParam("processInstanceIdIn", containing("testProcessInstanceId"))
            .withQueryParam("processInstanceIdIn", containing("testProcessInstanceId2"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task), TaskDto.fromEntity(task2)))))
        )
    );
  }

  @Test
  public void shouldReturnTaskCount() {
    CountResultDto taskCount = camundaTaskRestClient
        .getTaskCountByParams(TaskQueryDto.builder().build());

    assertThat(taskCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfTasks() {
    List<TaskDto> tasksByParams = camundaTaskRestClient
        .getTasksByParams(TaskQueryDto.builder().assignee("testAssignee").build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getAssignee()).isEqualTo("testAssignee");
  }

  @Test
  public void shouldReturnTaskById() {
    TaskDto taskById = camundaTaskRestClient.getTaskById("tid");

    assertThat(taskById.getId()).isEqualTo("tid");
  }

  @Test
  public void shouldReturn403TaskById() {
    AuthorizationException exception = assertThrows(AuthorizationException.class,
        () -> camundaTaskRestClient.getTaskById("tid403"));

    assertThat(exception.getType()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message403");
  }

  @Test
  public void shouldReturn404TaskById() {
    TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
        () -> camundaTaskRestClient.getTaskById("tid404"));

    assertThat(exception.getType()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message404");
  }

  @Test
  public void shouldCompleteTaskById() {
    Map<String, VariableValueDto> variables = camundaTaskRestClient
        .completeTaskById("testId", new CompleteTaskDto());

    assertThat(variables).isNotEmpty();
  }

  @Test
  public void shouldCompleteTaskByIdSuccessfulWhenHttpStatus204() {
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/task/testId204/complete"))
            .willReturn(aResponse()
                .withStatus(204)
                .withHeader("Content-Type", "application/json")))
    );
    Map<String, VariableValueDto> variables = camundaTaskRestClient
        .completeTaskById("testId204", new CompleteTaskDto());

    assertThat(variables).isNull();
  }

  @Test
  public void shouldReturnTasksByProcessInstanceIdIn() {
    List<TaskDto> tasksByParams = camundaTaskRestClient
        .getTasksByParams(TaskQueryDto.builder()
            .processInstanceIdIn(
                Lists.newArrayList("testProcessInstanceId", "testProcessInstanceId2")).build());

    assertThat(tasksByParams.size()).isEqualTo(2);
    assertThat(
        tasksByParams.stream()
            .anyMatch(t -> "testProcessInstanceId".equals(t.getProcessInstanceId()))).isTrue();
    assertThat(
        tasksByParams.stream()
            .anyMatch(t -> "testProcessInstanceId2".equals(t.getProcessInstanceId()))).isTrue();
  }

  @Test
  public void shouldReturnTasksByProcessInstanceId() throws JsonProcessingException {
    TaskEntity task = new TaskEntity();
    task.setProcessInstanceId("testProcessInstanceId");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task"))
            .withQueryParam("processInstanceId", equalTo("testProcessInstanceId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task)))))
        )
    );

    List<TaskDto> tasksByParams = camundaTaskRestClient
        .getTasksByParams(TaskQueryDto.builder()
            .processInstanceId("testProcessInstanceId").build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getProcessInstanceId()).isEqualTo("testProcessInstanceId");
  }

  @Test
  public void shouldReturn422DuringTaskCompletion() throws JsonProcessingException {
    ErrorDetailsDto details = new ErrorDetailsDto();
    details.setValidationErrors(Lists.newArrayList(new ValidationErrorDto("test msg",
        Maps.newHashMap("key1", "val1"))));
    UserDataValidationErrorDto errorDto422 = UserDataValidationErrorDto.builder().details(details).build();
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/taskId/complete"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(422)
                .withBody(objectMapper.writeValueAsString(errorDto422)))
        )
    );

    UserDataValidationException exception = assertThrows(UserDataValidationException.class,
        () -> camundaTaskRestClient.completeTaskById("taskId", new CompleteTaskDto()));

    assertThat(exception.getError().getDetails().getValidationErrors().get(0).getMessage()).isEqualTo("test msg");
    assertThat(exception.getError().getDetails().getValidationErrors().get(0).getContext().get("key1")).isEqualTo("val1");
  }
}
