package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import java.util.Map;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
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

  @Test
  public void shouldReturnTaskCount() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );

    var taskCount = camundaTaskRestClient.getTaskCountByParams(TaskQueryDto.builder().build());

    assertThat(taskCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfTasks() throws JsonProcessingException {
    var taskDto = new TaskDto();
    taskDto.setAssignee("testAssignee");
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(taskDto))))
        )
    );

    var tasksByParams = camundaTaskRestClient
        .getTasksByParams(TaskQueryDto.builder().assignee("testAssignee").build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getAssignee()).isEqualTo("testAssignee");
  }

  @Test
  public void shouldReturnTaskById() throws JsonProcessingException {
    var taskDtoById = new TaskDto();
    taskDtoById.setId("tid");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(taskDtoById)))
        )
    );

    var taskById = camundaTaskRestClient.getTaskById("tid");

    assertThat(taskById.getId()).isEqualTo("tid");
  }

  @Test
  public void shouldReturn403TaskById() throws JsonProcessingException {
    var errorDto403 = new ErrorDto("type", "message403");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid403"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(403)
                .withBody(objectMapper.writeValueAsString(errorDto403)))
        )
    );

    var exception = assertThrows(AuthorizationException.class,
        () -> camundaTaskRestClient.getTaskById("tid403"));

    assertThat(exception.getType()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message403");
  }

  @Test
  public void shouldReturn404TaskById() throws JsonProcessingException {
    var errorDto404 = new ErrorDto("type", "message404");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid404"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(404)
                .withBody(objectMapper.writeValueAsString(errorDto404)))
        )
    );

    var exception = assertThrows(TaskNotFoundException.class,
        () -> camundaTaskRestClient.getTaskById("tid404"));

    assertThat(exception.getType()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message404");
  }

  @Test
  public void shouldCompleteTaskById() throws JsonProcessingException {
    Map<String, VariableValueDto> completeVariables = new HashMap<>();
    completeVariables.put("var1", new VariableValueDto());
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/task/testId/complete"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(completeVariables))))
    );

    var variables = camundaTaskRestClient.completeTaskById("testId", new CompleteTaskDto());

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
    var variables = camundaTaskRestClient.completeTaskById("testId204", new CompleteTaskDto());

    assertThat(variables).isNull();
  }

  @Test
  public void shouldReturnTasksByProcessInstanceIdIn() throws JsonProcessingException {
    var requestDto = TaskQueryDto.builder()
        .processInstanceIdIn(
            Lists.newArrayList("testProcessInstanceId", "testProcessInstanceId2")).build();

    var task = new TaskEntity();
    task.setProcessInstanceId("testProcessInstanceId");
    var task2 = new TaskEntity();
    task2.setProcessInstanceId("testProcessInstanceId2");
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task), TaskDto.fromEntity(task2)))))
        )
    );

    var tasksByParams = camundaTaskRestClient.getTasksByParams(requestDto);

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
    var requestDto = TaskQueryDto.builder().processInstanceId("testProcessInstanceId").build();

    var task = new TaskEntity();
    task.setProcessInstanceId("testProcessInstanceId");
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(requestDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task)))))
        )
    );

    var tasksByParams = camundaTaskRestClient.getTasksByParams(TaskQueryDto.builder()
        .processInstanceId("testProcessInstanceId").build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getProcessInstanceId()).isEqualTo("testProcessInstanceId");
  }

  @Test
  public void shouldReturn422DuringTaskCompletion() throws JsonProcessingException {
    var details = new ErrorDetailsDto();
    details.setErrors(Lists.newArrayList(new ValidationErrorDto("test msg",
        "key1", "val1")));
    var errorDto422 = UserDataValidationErrorDto.builder().details(details).build();
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/taskId/complete"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(422)
                .withBody(objectMapper.writeValueAsString(errorDto422)))
        )
    );

    var completeTaskDto = new CompleteTaskDto();
    var exception = assertThrows(UserDataValidationException.class,
        () -> camundaTaskRestClient.completeTaskById("taskId", completeTaskDto));

    assertThat(exception.getDetails().getErrors().get(0).getMessage())
        .isEqualTo("test msg");
    assertThat(exception.getDetails().getErrors().get(0).getField())
        .isEqualTo("key1");
    assertThat(exception.getDetails().getErrors().get(0).getValue())
        .isEqualTo("val1");
  }
}
