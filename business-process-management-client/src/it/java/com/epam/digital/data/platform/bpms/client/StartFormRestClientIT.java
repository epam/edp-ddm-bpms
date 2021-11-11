package com.epam.digital.data.platform.bpms.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.StartFormQueryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StartFormRestClientIT extends BaseIT {

  @Autowired
  private StartFormRestClient startFormRestClient;

  @Test
  void shouldReturnTaskProperties() throws JsonProcessingException {
    var startForms = Map.of("process-definition", "start-form");
    var startFormQueryDto = StartFormQueryDto.builder()
        .processDefinitionIdIn(Set.of("process-definition")).build();

    restClientWireMock.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/extended/start-form"))
            .withRequestBody(equalTo(objectMapper.writeValueAsString(startFormQueryDto)))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(objectMapper.writeValueAsString(startForms))
            )
        ));

    var result = startFormRestClient.getStartFormKeyMap(startFormQueryDto);

    assertThat(result).isEqualTo(startForms);
  }
}
