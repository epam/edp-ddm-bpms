package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

public class SubjectDetailEdrRegistryConnectorDelegateIT extends BaseBpmnIT {

  @Inject
  @Qualifier("trembitaMockServer")
  protected WireMockServer trembitaMockServer;


  @Test
  @Deployment(resources = {"bpmn/testSubjectDetailEdrRegistryConnectorDelegate.bpmn"})
  public void shouldSearchSubjects() throws IOException, URISyntaxException {
    mockSubjectDetail();

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_subject_detail_key");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }

  private void mockSubjectDetail() throws IOException, URISyntaxException {
    String response = Files.readString(
        Paths.get(TestUtils.class.getResource("/xml/subjectDetailResponse.xml").toURI()),
        StandardCharsets.UTF_8);

    trembitaMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(".*SubjectDetail.*"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }
}
