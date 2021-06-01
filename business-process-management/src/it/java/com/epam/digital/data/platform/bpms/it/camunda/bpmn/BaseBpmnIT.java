package com.epam.digital.data.platform.bpms.it.camunda.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpms.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.bpms.it.BaseIT;
import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseBpmnIT extends BaseIT {

  private final String MOCK_SERVER = "/mock-server";
  private final String SETTINGS_MOCK_SERVER = "/user-settings-mock-server";

  @Inject
  @Qualifier("digitalSignatureMockServer")
  protected WireMockServer digitalSignatureMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  protected WireMockServer dataFactoryMockServer;
  @Inject
  @Qualifier("userSettingsWireMock")
  protected WireMockServer userSettingsWireMock;
  @Value("${ceph.bucket}")
  protected String cephBucketName;

  @Inject
  protected ObjectMapper objectMapper;
  @Inject
  protected CephKeyProvider cephKeyProvider;

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();
  private final Map<String, Object> expectedCephStorage = new HashMap<>();

  @Before
  public void init() {
    cephService.clearStorage();
    formDataCephService.clearStorage();
    expectedVariablesMap.clear();
    expectedCephStorage.clear();
    runtimeService.createProcessInstanceQuery().list().forEach(
        processInstance -> runtimeService
            .deleteProcessInstance(processInstance.getId(), "test clear"));
  }

  protected void completeTask(String taskId, String processInstanceId, String formData)
      throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskId, processInstanceId);
    formDataCephService.putFormData(cephKey, deserializeFormData(TestUtils.getContent(formData)));
    String id = taskService.createTaskQuery().taskDefinitionKey(taskId).singleResult().getId();
    taskService.complete(id);
  }

  protected void stubDataFactorySearch(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource()).build()
        .toUri();
    MappingBuilder mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getQueryParams()
        .forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryCreate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource()).build()
        .toUri();
    MappingBuilder mappingBuilder = post(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryRead(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryUpdate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromUri(URI.create(MOCK_SERVER)).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = put(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDataFactoryGet(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromPath(MOCK_SERVER)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode().build().toUri();
    var mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(stubData.getResponse())));
    dataFactoryMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubUserSettingsRead(StubData stubData) throws IOException {
    var uri = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER)
        .pathSegment(stubData.getResource(), stubData.getResourceId()).encode().build().toUri();
    var mappingBuilder = get(urlPathEqualTo(uri.getPath()))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(stubData.getResponse())));
    userSettingsWireMock.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubUserSettingsUpdate(StubData data) throws IOException {
    var uri = UriComponentsBuilder.fromUri(URI.create(SETTINGS_MOCK_SERVER)).pathSegment(data.getResource())
        .pathSegment(data.getResourceId()).build().toUri();
    MappingBuilder mappingBuilder = put(urlPathEqualTo(uri.getPath()))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    userSettingsWireMock.addStubMapping(stubFor(mappingBuilder));
  }

  protected void stubDigitalSignature(StubData data) throws IOException {
    MappingBuilder mappingBuilder = post(urlPathEqualTo("/api/eseal/sign"))
        .withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())))
        .willReturn(aResponse().withStatus(200).withBody(TestUtils.getContent(data.getResponse())));

    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));

    digitalSignatureMockServer.addStubMapping(stubFor(mappingBuilder));
  }

  protected void assertCephContent() {
    expectedCephStorage.forEach((key, value) -> {
      var actualMap = formDataCephService.getFormData(key);

      Assertions.assertThat(actualMap).isEqualTo(value);
    });
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(formData, FormDataDto.class);
    } catch (JsonProcessingException var4) {
      var4.clearLocation();
      throw new IllegalStateException("Couldn't deserialize form data", var4);
    }
  }

  protected void addExpectedCephContent(String processInstanceId, String taskDefinitionKey,
      String cephContent) throws IOException {
    var cephKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    expectedCephStorage.put(cephKey, deserializeFormData(TestUtils.getContent(cephContent)));
  }

  protected void assertWaitingActivity(ProcessInstance processInstance,
      String searchLabFormActivityDefinitionKey, String formKey) {
    assertThat(processInstance).isWaitingAt(searchLabFormActivityDefinitionKey);
    assertThat(task(searchLabFormActivityDefinitionKey)).hasFormKey(formKey);
    assertThat(processInstance).variables().hasSize(expectedVariablesMap.size())
        .containsAllEntriesOf(expectedVariablesMap);
    assertCephContent();
  }
}
