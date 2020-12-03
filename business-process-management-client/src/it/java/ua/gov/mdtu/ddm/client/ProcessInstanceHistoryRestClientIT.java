package ua.gov.mdtu.ddm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.util.Date;

import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import mdtu.ddm.lowcode.api.dto.HistoryProcessInstanceQueryDto;
import mdtu.ddm.lowcode.api.dto.enums.SortOrder;

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
        stubFor(get(urlEqualTo("/api/history/process-instance?sortOrder=asc&unfinished=true&sortBy=startTime"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(
                    objectMapper.writeValueAsString(Lists.newArrayList(historicProcessInstanceDto))))
        )
    );
  }

  @Test
  public void shouldReturnListOfHistoryProcessInstances() {
    //when
    var processInstances = processInstanceHistoryRestClient.getProcessInstances(
        HistoryProcessInstanceQueryDto.builder().unfinished(true)
            .sortBy(HistoryProcessInstanceQueryDto.SortByConstants.SORT_BY_START_TIME)
            .sortOrder(SortOrder.ASC.stringValue()).build()
    );
    //then
    assertThat(processInstances.size()).isOne();
    assertThat(processInstances.get(0).getId()).isEqualTo("id");
    assertThat(processInstances.get(0).getProcessDefinitionId()).isEqualTo("processDefinitionId");
    assertThat(processInstances.get(0).getProcessDefinitionName()).isEqualTo("processDefinitionName");
    assertThat(processInstances.get(0).getStartTime()).isEqualTo(new Date(10000L));
  }
}
