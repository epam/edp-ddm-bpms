/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpm.it.bpmn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.historyService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.managementService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import com.epam.digital.data.platform.bpm.it.builder.StubData;
import com.epam.digital.data.platform.bpm.it.dto.CompleteActivityDto;
import com.epam.digital.data.platform.bpm.it.util.CamundaAssertionUtil;
import com.epam.digital.data.platform.bpm.it.util.TestUtils;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseBpmnIT extends BaseIT {

  private static final String MOCK_SERVER = "/mock-server";
  private static final String SETTINGS_MOCK_SERVER = "/user-settings-mock-server";
  protected static final String EXCERPT_SERVICE_MOCK_SERVER = "/excerpt-mock-service";
  public static final String START_FORM_CEPH_KEY = "startFormCephKey";

  @Inject
  @Qualifier("digitalSignatureMockServer")
  protected WireMockServer digitalSignatureMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  protected WireMockServer dataFactoryMockServer;
  @Inject
  @Qualifier("userSettingsWireMock")
  protected WireMockServer userSettingsWireMock;
  @Inject
  @Qualifier("excerptServiceWireMock")
  protected WireMockServer excerptServiceWireMock;
  @Inject
  @Qualifier("trembitaMockServer")
  protected WireMockServer trembitaMockServer;
  @Inject
  @Qualifier("documentServiceWireMock")
  protected WireMockServer documentServiceWireMock;
  @Value("${ceph.bucket}")
  protected String cephBucketName;

  protected FormDataKeyProvider cephKeyProvider;

  protected String testUserName = "testuser";
  protected String testUserToken;

  protected final Map<String, Object> expectedVariablesMap = new HashMap<>();

  @Before
  public void init() {
    expectedVariablesMap.clear();
    testUserToken = TestUtils.getContent("/json/testuserAccessToken.json");
    cephKeyProvider = new FormDataKeyProviderImpl();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(testUserName, testUserToken));
    CamundaAssertionUtil.setFromDataStorageService(formDataStorageService);
    stubDocumentsDeleting();
  }

  @After
  public void destroy() {
    digitalSignatureMockServer.resetAll();
    dataFactoryMockServer.resetAll();
    userSettingsWireMock.resetAll();
    keycloakMockServer.resetAll();
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  protected void completeTask(CompleteActivityDto completeActivityDto) {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    saveFormDataToCeph(completeActivityDto);

    var url = String.format("api/task/%s/complete", task(activityDefinitionId).getId());
    postForNoContent(url, "{}", completeActivityDto.getCompleterAccessToken());
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Map<String, List<Map<String, String>>>> completeTaskWithError(
      CompleteActivityDto completeActivityDto) throws JsonProcessingException {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    saveFormDataToCeph(completeActivityDto);

    var url = String.format("api/task/%s/complete", task(activityDefinitionId).getId());
    return (Map<String, Map<String, List<Map<String, String>>>>) postForObject(url, "{}", Map.class,
        completeActivityDto.getCompleterAccessToken());
  }

  private void saveFormDataToCeph(CompleteActivityDto completeActivityDto) {
    var activityDefinitionId = completeActivityDto.getActivityDefinitionId();
    var processInstanceId = completeActivityDto.getProcessInstanceId();
    var cephKey = cephKeyProvider.generateKey(activityDefinitionId, processInstanceId);
    cephService.put(cephBucketName, cephKey,
        TestUtils.getContent(completeActivityDto.getExpectedFormData()));
  }

  protected void stubDataFactoryRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath(MOCK_SERVER)
        .pathSegment(data.getResource().split("/"));

    dataFactoryMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubDigitalSignatureRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath("/api/eseal/sign");

    digitalSignatureMockServer.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubSettingsRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER + "/api/settings")
        .pathSegment(data.getResource());

    userSettingsWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubExcerptServiceRequest(StubData data) {
    var uriBuilder = Objects.nonNull(data.getUri()) ? data.getUri() :
        UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER).pathSegment(data.getResource());
    excerptServiceWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  private MappingBuilder getMappingBuilder(StubData data, UriComponentsBuilder uriBuilder) {
    if (Objects.nonNull(data.getResourceId())) {
      uriBuilder.pathSegment(data.getResourceId());
    }

    var mappingBuilder = getMappingBuilderForMethod(data.getHttpMethod(),
        uriBuilder.encode().toUriString());
    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    mappingBuilder.withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE));

    data.getQueryParams()
        .forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    if (Objects.nonNull(data.getRequestBody())) {
      mappingBuilder.withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())));
    }

    mappingBuilder
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(TestUtils.getContent(data.getResponse())));
    return mappingBuilder;
  }

  private MappingBuilder getMappingBuilderForMethod(HttpMethod method, String uri) {
    switch (method) {
      case GET:
        return get(urlPathEqualTo(uri));
      case POST:
        return post(urlPathEqualTo(uri));
      case PUT:
        return put(urlPathEqualTo(uri));
      case DELETE:
        return delete(urlPathEqualTo(uri));
      default:
        throw new NullPointerException("Stub method isn't defined");
    }
  }

  @SneakyThrows
  protected void stubSearchSubjects(String responseXmlFilePath) {
    stubTrembita(responseXmlFilePath, "SearchSubjects");
  }

  private void stubTrembita(String responseXmlFilePath, String serviceCode) throws Exception {
    String response = Files.readString(
        Paths.get(TestUtils.class.getResource(responseXmlFilePath).toURI()),
        StandardCharsets.UTF_8);

    trembitaMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(String.format(".*%s.*", serviceCode)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(TestUtils.getContent(formData), FormDataDto.class);
    } catch (JsonProcessingException var4) {
      throw new IllegalStateException("Couldn't deserialize form data", var4);
    }
  }

  protected void addExpectedVariable(String name, Object value) {
    expectedVariablesMap.put(name, value);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceWithError(
      String processDefinitionKey, String token)
      throws JsonProcessingException {
    return (Map<String, Map<String, List<Map<String, String>>>>) postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        "{}", Map.class, token);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Map<String, List<Map<String, String>>>> startProcessInstanceWithStartFormForError(
      String processDefinitionKey, String token, FormDataDto formDataDto)
      throws JsonProcessingException {
    var resultMap = startProcessInstanceWithStartForm(processDefinitionKey, token,
        formDataDto);
    return (Map<String, Map<String, List<Map<String, String>>>>) resultMap;
  }

  protected String startProcessInstance(String processDefinitionKey, String token)
      throws JsonProcessingException {
    var result = postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        "{}", Map.class, token);

    return (String) result.get("id");
  }

  protected String startProcessInstance(String processDefinitionKey,
      StartProcessInstanceDto body, String token) throws JsonProcessingException {
    var bodyAsStr = objectMapper.writeValueAsString(body);
    var result = postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        bodyAsStr, Map.class, token);

    return (String) result.get("id");
  }

  protected String startProcessInstanceWithStartFormAndGetId(String processDefinitionKey,
      String token, FormDataDto formDataDto) throws JsonProcessingException {
    var result = startProcessInstanceWithStartForm(processDefinitionKey, token, formDataDto);
    return (String) result.get("id");
  }

  protected Map startProcessInstanceWithStartForm(String processDefinitionKey, String token,
      FormDataDto formDataDto) throws JsonProcessingException {
    formDataStorageService.putFormData(START_FORM_CEPH_KEY, formDataDto);

    var startProcessInstanceDto = new StartProcessInstanceDto();
    var variableValueDto = new VariableValueDto();
    variableValueDto.setValue(START_FORM_CEPH_KEY);
    startProcessInstanceDto.setVariables(
        Map.of(StartFormCephKeyVariable.START_FORM_CEPH_KEY_VARIABLE_NAME, variableValueDto));

    return postForObject(
        String.format("api/process-definition/key/%s/start", processDefinitionKey),
        objectMapper.writeValueAsString(startProcessInstanceDto), Map.class, token);
  }

  protected void assertSystemSignature(String processInstanceId, String variableName,
      String cephContent) {
    var variables = historyService().createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId).list();

    var signatureCephKeyVar = variables.stream()
        .filter(variable -> variable.getName().equals(variableName)).findAny();
    Assertions.assertThat(signatureCephKeyVar).isNotEmpty();

    var signatureCephKey = (String) signatureCephKeyVar.get().getValue();
    Assertions.assertThat(signatureCephKey)
        .matches("lowcode_" + processInstanceId + "_.+_system_signature_ceph_key");

    assertSignature(signatureCephKey, cephContent);
  }

  protected void assertSystemSignatureBathCreationForOneOperation(String processInstanceId,
      String cephContent) {
    var systemSignatureCephKey = String
        .format("lowcode_%s_system_signature_ceph_key_0", processInstanceId);
    assertSignature(systemSignatureCephKey, cephContent);
  }

  @SneakyThrows
  private void assertSignature(String systemSignatureCephKey, String cephContent) {
    var signature = cephService.getAsString(cephBucketName, systemSignatureCephKey);
    Assertions.assertThat(signature).isNotEmpty();

    var signatureMap = objectMapper.readerForMapOf(Object.class).readValue(signature.get());
    var expectedSignatureMap = objectMapper.readerForMapOf(Object.class)
        .readValue(TestUtils.getContent(cephContent));
    Assertions.assertThat(signatureMap).isEqualTo(expectedSignatureMap);
  }

  protected void executeWaitingJob(String activityDefinitionId) {
    var jobs = managementService().createJobQuery().activityId(activityDefinitionId).list();
    Assertions.assertThat(jobs).hasSize(1);
    jobs.forEach(job -> managementService().executeJob(job.getId()));
  }


  @SneakyThrows
  protected String addFieldToFormDataAndReturn(String form, String fieldName, Object filedValue) {
    var formData = TestUtils.getContent(form);
    var formDataMap = objectMapper.readValue(formData, Map.class);
    ((Map) formDataMap.get("data")).put(fieldName, filedValue);
    return objectMapper.writeValueAsString(formDataMap);
  }

  @SneakyThrows
  protected String addFiledToJson(String json, String fieldName, Object filedValue) {
    var data = TestUtils.getContent(json);
    var formDataMap = objectMapper.readValue(data, Map.class);
    formDataMap.put(fieldName, filedValue);
    return objectMapper.writeValueAsString(formDataMap);
  }

  @SneakyThrows
  protected String addFiledToSignatureFormData(String json, String fieldName, Object filedValue) {
    var data = TestUtils.getContent(json);
    var jsonDataMap = objectMapper.readValue(data, Map.class);
    var dataMap = objectMapper.readValue((String) jsonDataMap.get("data"), Map.class);
    dataMap.put(fieldName, filedValue);
    var dataMapStr = objectMapper.writeValueAsString(dataMap);
    jsonDataMap.put("data", dataMapStr);
    return objectMapper.writeValueAsString(jsonDataMap);
  }

  protected void stubDocumentsDeleting() {
    documentServiceWireMock.addStubMapping(
        stubFor(delete(urlPathMatching(".*/documents/.*"))
            .willReturn(aResponse().withStatus(200))));
  }
}
