package com.epam.digital.data.platform.bpms.it;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class ExcerptConnectorStatusDelegateIT extends BaseBpmnIT {

  @Test
  public void shouldGetExcerptStatus() throws IOException {
    var uri = UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER)
        .pathSegment("excerpts").pathSegment("123456789").pathSegment("status");

    stubExcerptServiceRequest(StubData.builder()
        .response("{\"status\": \"COMPLETED\"}")
        .httpMethod(HttpMethod.GET)
        .uri(uri)
        .build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_get_excerpt_status");

    var processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
