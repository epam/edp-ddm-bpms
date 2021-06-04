package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

public class SearchSubjectsEdrRegistryConnectorDelegateIT extends BaseBpmnIT {

  @Inject
  @Qualifier("trembitaMockServer")
  protected WireMockServer trembitaMockServer;


  @Test
  @Deployment(resources = {"bpmn/testSearchSubjectsEdrRegistryConnectorDelegate.bpmn"})
  public void shouldSearchSubjects() throws IOException {
    mockSearchSubjects();

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_search_subject_key");

    completeTask("Activity_1f4byzi", processInstance.getId(), "{}");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }

  private void mockSearchSubjects() throws IOException {
    String response = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/xml/searchSubjectsResponse.xml")));

    trembitaMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(".*SearchSubjects.*"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }
}
