package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.SignableUserTaskDto;
import com.epam.digital.data.platform.dso.api.dto.Subject;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ExtendedUserTaskRestClientIT extends BaseIT {

  @Autowired
  private ExtendedUserTaskRestClient extendedUserTaskRestClient;

  @Test
  public void getUserTaskById() throws JsonProcessingException {
    var id = "taskId";
    var processDefinitionName = "processDefinitionName";
    var signatureValidationPack = Set.of(Subject.ENTREPRENEUR);
    var dto = new SignableUserTaskDto();
    dto.setId(id);
    dto.setProcessDefinitionName(processDefinitionName);
    dto.setSignatureValidationPack(signatureValidationPack);
    restClientWireMock.addStubMapping(
        stubFor(get(urlPathEqualTo("/api/extended/task/taskId"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(dto))))
    );

    var result = extendedUserTaskRestClient.getUserTaskById(id);

    assertThat(result)
        .hasFieldOrPropertyWithValue("id", id)
        .hasFieldOrPropertyWithValue("processDefinitionName", processDefinitionName)
        .hasFieldOrPropertyWithValue("signatureValidationPack", signatureValidationPack);
  }
}
