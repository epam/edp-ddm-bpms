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

package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.api.dto.DdmClaimTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCompleteTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmCompletedTaskDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskCountQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmVariableValueDto;
import com.epam.digital.data.platform.bpms.api.dto.PaginationQueryDto;
import com.epam.digital.data.platform.bpms.client.exception.AuthorizationException;
import com.epam.digital.data.platform.bpms.client.exception.TaskNotFoundException;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TaskRestClientIT extends BaseIT {

  @Autowired
  private TaskRestClient taskRestClient;

  @Test
  void shouldReturnTaskCount() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );

    var taskCount = taskRestClient.getTaskCountByParams(DdmTaskCountQueryDto.builder().build());

    assertThat(taskCount.getCount()).isOne();
  }

  @Test
  void shouldReturnListOfTasks() throws JsonProcessingException {
    var paginationQueryDto = PaginationQueryDto.builder().build();
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

    var tasksByParams = taskRestClient
        .getTasksByParams(DdmTaskQueryDto.builder().assignee("testAssignee").build(),
            paginationQueryDto);

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getAssignee()).isEqualTo("testAssignee");
  }

  @Test
  void shouldReturnTaskById() throws JsonProcessingException {
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

    var taskById = taskRestClient.getTaskById("tid");

    assertThat(taskById.getId()).isEqualTo("tid");
  }

  @Test
  void shouldReturn403TaskById() throws JsonProcessingException {
    var errorDto403 = new SystemErrorDto("testTraceId", "type", "message403", "testLocalizedMsg");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid403"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(403)
                .withBody(objectMapper.writeValueAsString(errorDto403)))
        )
    );

    var exception = assertThrows(AuthorizationException.class,
        () -> taskRestClient.getTaskById("tid403"));

    assertThat(exception.getTraceId()).isEqualTo("testTraceId");
    assertThat(exception.getCode()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message403");
    assertThat(exception.getLocalizedMessage()).isEqualTo("testLocalizedMsg");
  }

  @Test
  void shouldReturn404TaskById() throws JsonProcessingException {
    var errorDto404 = new SystemErrorDto("testTraceId", "type", "message404", "testLocalizedMsg");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/task/tid404"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(404)
                .withBody(objectMapper.writeValueAsString(errorDto404)))
        )
    );

    var exception = assertThrows(TaskNotFoundException.class,
        () -> taskRestClient.getTaskById("tid404"));

    assertThat(exception.getTraceId()).isEqualTo("testTraceId");
    assertThat(exception.getCode()).isEqualTo("type");
    assertThat(exception.getMessage()).isEqualTo("message404");
    assertThat(exception.getLocalizedMessage()).isEqualTo("testLocalizedMsg");
  }

  @Test
  void shouldCompleteTaskById() {
    var requestBody = "{\"withVariablesInReturn\":true, "
        + "\"variables\":{\"inputVar\":{\"value\":\"inputVariable\",\"type\":null,\"valueInfo\":null}}}";
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/extended/task/testId/complete"))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"id\", \"processInstanceId\":\"processInstanceId\","
                    + "\"rootProcessInstanceId\":\"rootProcessInstanceId\", "
                    + "\"rootProcessInstanceEnded\":false,"
                    + "\"variables\":{\"var1\":{\"value\":\"variable\"}}}")))
    );

    var expected = DdmCompletedTaskDto.builder()
        .id("id")
        .processInstanceId("processInstanceId")
        .rootProcessInstanceId("rootProcessInstanceId")
        .rootProcessInstanceEnded(false)
        .variables(Map.of("var1", DdmVariableValueDto.builder().value("variable").build()))
        .build();
    var actual = taskRestClient.completeTaskById("testId",
        DdmCompleteTaskDto.builder()
            .withVariablesInReturn(true)
            .variables(Map.of("inputVar",
                DdmVariableValueDto.builder().value("inputVariable").build()))
            .build());

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldCompleteTaskByIdSuccessfulWhenNoVariablesInReturn() {
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/extended/task/testId200/complete"))
            .withRequestBody(equalToJson("{\"withVariablesInReturn\":false,\"variables\":null}"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":\"id\", \"processInstanceId\":\"processInstanceId\","
                    + "\"rootProcessInstanceId\":\"rootProcessInstanceId\", "
                    + "\"rootProcessInstanceEnded\":true,\"variables\":{}}")))
    );

    var expected = DdmCompletedTaskDto.builder()
        .id("id")
        .processInstanceId("processInstanceId")
        .rootProcessInstanceId("rootProcessInstanceId")
        .rootProcessInstanceEnded(true)
        .variables(Map.of())
        .build();
    var actual = taskRestClient.completeTaskById("testId200",
        DdmCompleteTaskDto.builder().build());

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void shouldReturnTasksByProcessInstanceIdIn() throws JsonProcessingException {
    var paginationQueryDto = PaginationQueryDto.builder().build();
    var requestDto = DdmTaskQueryDto.builder()
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

    var tasksByParams = taskRestClient.getTasksByParams(requestDto, paginationQueryDto);

    assertThat(tasksByParams.size()).isEqualTo(2);
    assertThat(
        tasksByParams.stream()
            .anyMatch(t -> "testProcessInstanceId".equals(t.getProcessInstanceId()))).isTrue();
    assertThat(
        tasksByParams.stream()
            .anyMatch(t -> "testProcessInstanceId2".equals(t.getProcessInstanceId()))).isTrue();
  }

  @Test
  void shouldReturnTasksByProcessInstanceId() throws JsonProcessingException {
    var paginationQueryDto = PaginationQueryDto.builder().build();
    var requestDto = DdmTaskQueryDto.builder().processInstanceId("testProcessInstanceId").build();

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

    var tasksByParams = taskRestClient.getTasksByParams(DdmTaskQueryDto.builder()
        .processInstanceId("testProcessInstanceId").build(), paginationQueryDto);

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getProcessInstanceId()).isEqualTo("testProcessInstanceId");
  }

  @Test
  void shouldReturn422DuringTaskCompletion() throws JsonProcessingException {
    var details = new ErrorsListDto();
    details.setErrors(Lists.newArrayList(new ErrorDetailDto("test msg",
        "key1", "val1")));
    var errorDto422 = ValidationErrorDto.builder().details(details).build();
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/extended/task/taskId/complete"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(422)
                .withBody(objectMapper.writeValueAsString(errorDto422)))
        )
    );

    var completeTaskDto = DdmCompleteTaskDto.builder().build();
    var exception = assertThrows(ValidationException.class,
        () -> taskRestClient.completeTaskById("taskId", completeTaskDto));

    assertThat(exception.getDetails().getErrors().get(0).getMessage())
        .isEqualTo("test msg");
    assertThat(exception.getDetails().getErrors().get(0).getField())
        .isEqualTo("key1");
    assertThat(exception.getDetails().getErrors().get(0).getValue())
        .isEqualTo("val1");
  }

  @Test
  void shouldReturnTasksByOrQueries() throws JsonProcessingException {
    var paginationQueryDto = PaginationQueryDto.builder().build();
    String expectedBody = "{\"orQueries\":[{\"assignee\": \"testuser\",\"unassigned\": true}]}";

    var requestDto = DdmTaskQueryDto.builder()
        .orQueries(List.of(DdmTaskQueryDto.builder()
            .assignee("testuser")
            .unassigned(true)
            .build()))
        .build();

    var task = new TaskEntity();
    task.setId("testId");
    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/task"))
            .withRequestBody(equalToJson(expectedBody))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody((objectMapper.writeValueAsString(List.of(TaskDto.fromEntity(task))))))));

    List<TaskDto> tasks = taskRestClient.getTasksByParams(requestDto, paginationQueryDto);

    assertThat(tasks).isNotEmpty();
    assertThat(tasks.get(0).getId()).isEqualTo("testId");
  }

  @Test
  void shouldClaimTaskByIdSuccessfulWhenHttpStatus204() throws JsonProcessingException {
    var claimTaskDto = DdmClaimTaskQueryDto.builder().userId("userId").build();
    var stubMapping = stubFor(post(urlEqualTo("/api/task/testId204/claim"))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(claimTaskDto)))
        .willReturn(aResponse().withStatus(204)));
    restClientWireMock.addStubMapping(stubMapping);
    taskRestClient.claimTaskById("testId204", claimTaskDto);
    restClientWireMock.verify(newRequestPattern(RequestMethod.POST,
        new UrlPathPattern(equalTo("/api/task/testId204/claim"), false)));
  }

  @Test
  void shouldReturnTaskVariables() throws JsonProcessingException {
    var taskId = "taskId";
    var variableValue = "variableValue";
    var type = "String";
    var varValueDto = DdmVariableValueDto.builder()
        .type(type)
        .value(variableValue)
        .build();
    var expectedVariables = Map.of(variableValue, varValueDto);

    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo(String.format("/api/task/%s/variables", taskId)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(expectedVariables)))
        )
    );

    var result = taskRestClient.getTaskVariables(taskId);

    var resultVariables = result.get(variableValue);
    assertThat(resultVariables.getValue()).isEqualTo(variableValue);
    assertThat(resultVariables.getType()).isEqualTo(type);
  }
}
