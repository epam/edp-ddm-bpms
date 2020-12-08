package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.TaskQueryDto;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    Map<String, VariableValueDto> completeVariables = new HashMap<>();
    completeVariables.put("var1", new VariableValueDto());
    restClientWireMock.addStubMapping(
        stubFor(post(urlEqualTo("/api/task/testId/complete"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(completeVariables))))
    );
  }

  @Test
  public void shouldReturnTaskCount() {
    //when
    CountResultDto taskCount = camundaTaskRestClient
        .getTaskCountByParams(TaskQueryDto.builder().build());
    //then
    assertThat(taskCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnListOfTasks() {
    //when
    List<TaskDto> tasksByParams = camundaTaskRestClient
        .getTasksByParams(TaskQueryDto.builder().assignee("testAssignee").build());
    //then
    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getAssignee()).isEqualTo("testAssignee");
  }

  @Test
  public void shouldReturnTaskById() {
    //when
    TaskDto taskById = camundaTaskRestClient.getTaskById("tid");
    //then
    assertThat(taskById.getId()).isEqualTo("tid");
  }

  @Test
  public void shouldCompleteTaskById() {
    //when
    Map<String, VariableValueDto> variables = camundaTaskRestClient
        .completeTaskById("testId", new CompleteTaskDto());
    //then
    assertThat(variables).isNotEmpty();
  }
}
