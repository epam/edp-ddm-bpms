package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.HistoryVariableInstanceQueryDto;

public class HistoryVariableInstanceClientIT extends BaseIT {

  @Autowired
  private HistoryVariableInstanceClient historyVariableInstanceClient;

  @Test
  public void shouldReturnVariablesInstanceList() throws JsonProcessingException {
    HistoricVariableInstanceDto dto = new HistoricVariableInstanceDto();
    dto.setValue("value");
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/history/variable-instance"))
            .withQueryParam("variableName", equalTo("myVariable"))
            .withQueryParam("processInstanceId", equalTo("processInstance"))
            .withQueryParam("processInstanceIdIn", equalTo("processInstance1,processInstance2"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(Lists.newArrayList(dto))))
        )
    );

    List<HistoricVariableInstanceDto> tasksByParams = historyVariableInstanceClient
        .getList(HistoryVariableInstanceQueryDto.builder().variableName("myVariable")
            .processInstanceId("processInstance")
            .processInstanceIdIn(Lists.newArrayList("processInstance1", "processInstance2"))
            .build());

    assertThat(tasksByParams.size()).isOne();
    assertThat(tasksByParams.get(0).getValue()).isEqualTo("value");
  }
}
