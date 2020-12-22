package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Date;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryProcessInstanceQueryDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.enums.SortOrder;

public class ProcessInstanceHistoryRestClientIT extends BaseIT {

  @Autowired
  private ProcessInstanceHistoryRestClient processInstanceHistoryRestClient;

  @Before
  public void init() throws JsonProcessingException {
    //init list response
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("id");
    historicProcessInstanceEntity.setProcessDefinitionId("processDefinitionId");
    historicProcessInstanceEntity.setProcessDefinitionName("processDefinitionName");
    historicProcessInstanceEntity.setStartTime(new Date(10000L));
    var historicProcessInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/process-instance"))
            .withQueryParam("sortOrder", equalTo("asc"))
            .withQueryParam("unfinished", equalTo("true"))
            .withQueryParam("sortBy", equalTo("startTime"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper
                        .writeValueAsString(Lists.newArrayList(historicProcessInstanceDto))))
        )
    );

    // init list of finished process instance response
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/process-instance"))
            .withQueryParam("finished", equalTo("true"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper
                        .writeValueAsString(Lists.newArrayList(historicProcessInstanceDto))))
        )
    );
  }

  @Test
  public void shouldReturnListOfHistoryProcessInstances() {
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().unfinished(true)
            .sortBy(HistoryProcessInstanceQueryDto.SortByConstants.SORT_BY_START_TIME)
            .sortOrder(SortOrder.ASC.stringValue()).build()
    );

    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("processDefinitionId");
    assertThat(processInstances.get(0).getProcessDefinitionName())
        .isEqualTo("processDefinitionName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(10000L));
  }

  @Test
  public void shouldReturnListOfFinishedHistoryProcessInstances() {
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().finished(true).build()
    );

    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("processDefinitionId");
    assertThat(processInstances.get(0).getProcessDefinitionName())
        .isEqualTo("processDefinitionName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(10000L));
  }

  @Test
  public void shouldReturnProcessInstanceById() throws JsonProcessingException {
    var historicProcessInstanceEntity = new HistoricProcessInstanceEntity();
    historicProcessInstanceEntity.setId("testId");
    var historicProcessInstanceDto = HistoricProcessInstanceDto
        .fromHistoricProcessInstance(historicProcessInstanceEntity);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/process-instance/testId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(historicProcessInstanceDto))
            )
        )
    );

    var processInstances = processInstanceHistoryRestClient.getProcessInstanceById("testId");

    assertThat(processInstances.getId()).isEqualTo("testId");
  }
}
