package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryTaskQueryDto;

public class HistoryTaskRestClientIT extends BaseIT {

  @Autowired
  private HistoryTaskRestClient historyTaskRestClient;

  @Test
  public void shouldReturnHistoryTasks() throws JsonProcessingException {
    TaskEntity task = new TaskEntity();
    task.setId("testId");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/task"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(
                        Lists.newArrayList(TaskDto.fromEntity(task)))))
        )
    );

    List<HistoricTaskInstanceEntity> tasksByParams = historyTaskRestClient
        .getHistoryTasksByParams(HistoryTaskQueryDto.builder().build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getId()).isEqualTo("testId");
  }
}
