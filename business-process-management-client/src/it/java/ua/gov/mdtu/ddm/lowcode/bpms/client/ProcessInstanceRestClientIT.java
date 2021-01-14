package ua.gov.mdtu.ddm.lowcode.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.ProcessInstanceVariableNotFoundException;

public class ProcessInstanceRestClientIT extends BaseIT {

  @Autowired
  private ProcessInstanceRestClient processInstanceRestClient;

  @Test
  public void shouldReturnProcessInstancesCount() throws JsonProcessingException {
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo("/api/process-instance/count"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(new CountResultDto(1L))))
        )
    );

    CountResultDto processInstancesCount = processInstanceRestClient.getProcessInstancesCount();

    assertThat(processInstancesCount.getCount()).isOne();
  }

  @Test
  public void shouldReturnProcessInstanceVariable() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var variableName = "variableName";
    var variableValue = "variableValue";

    var url = String
        .format("/api/process-instance/%s/variables/%s", processInstanceId, variableName);
    var varValueDto = new VariableValueDto();
    varValueDto.setValue(variableValue);
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo(url))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(varValueDto)))
        )
    );

    var result = processInstanceRestClient
        .getProcessInstanceVariable(processInstanceId, variableName);

    assertThat(result.getValue()).isEqualTo(variableValue);
  }

  @Test
  public void shouldThrowProcessInstanceVariableNotFound() {
    var processInstanceId = "processInstanceId";
    var variableName = "variableName404";

    var url = String
        .format("/api/process-instance/%s/variables/%s", processInstanceId, variableName);
    restClientWireMock.addStubMapping(
        stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(404))));

    assertThrows(ProcessInstanceVariableNotFoundException.class,
        () -> processInstanceRestClient
            .getProcessInstanceVariable(processInstanceId, variableName));
  }

  @Test
  public void shouldSaveProcessInstanceVariable() throws JsonProcessingException {
    var processInstanceId = "processInstanceId";
    var variableName = "variableName";
    var variableValue = "variableValue";

    var url = String
        .format("/api/process-instance/%s/variables/%s", processInstanceId, variableName);
    var varValueDto = new VariableValueDto();
    varValueDto.setValue(variableValue);
    restClientWireMock.addStubMapping(
        stubFor(put(urlEqualTo(url))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(varValueDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(varValueDto)))
        )
    );

    processInstanceRestClient
        .putProcessInstanceVariable(processInstanceId, variableName, varValueDto);

    UrlPattern lowcodeKeyUrlPattern = new UrlPattern(new EqualToPattern(url), false);
    restClientWireMock.verify(1, newRequestPattern(RequestMethod.PUT, lowcodeKeyUrlPattern));
  }
}
