package com.epam.digital.data.platform.bpms.extension.it;

import com.epam.digital.data.platform.bpms.extension.it.builder.StubData;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class ExcerptConnectorStatusDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/connector/testExcerptConnectorStatusDelegate.bpmn")
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
