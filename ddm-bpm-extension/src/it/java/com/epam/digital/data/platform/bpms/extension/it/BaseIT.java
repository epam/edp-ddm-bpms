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

package com.epam.digital.data.platform.bpms.extension.it;

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

import com.epam.digital.data.platform.bpms.extension.it.builder.StubData;
import com.epam.digital.data.platform.bpms.extension.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.extension.it.util.TestUtils;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.TimeZone;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@EnableFeignClients
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  private static final String SETTINGS_MOCK_SERVER = "/user-settings-mock-server";
  protected static final String EXCERPT_SERVICE_MOCK_SERVER = "/excerpt-mock-service";

  @Inject
  protected RuntimeService runtimeService;
  @Inject
  protected HistoryService historyService;
  @Inject
  protected TaskService taskService;
  @Inject
  protected ProcessEngine engine;
  @Inject
  protected ObjectMapper objectMapper;
  @Inject
  protected TestCephServiceImpl cephService;
  @Inject
  protected FormDataStorageService formDataStorageService;
  protected FormDataKeyProvider cephKeyProvider;
  @Inject
  private AuthorizationService authorizationService;
  @Inject
  @Qualifier("keycloakMockServer")
  protected WireMockServer keycloakMockServer;
  @Inject
  @Qualifier("excerptServiceWireMock")
  protected WireMockServer excerptServiceWireMock;
  @Inject
  @Qualifier("userSettingsWireMock")
  protected WireMockServer userSettingsWireMock;
  @Inject
  @Qualifier("trembitaMockServerEdr")
  protected WireMockServer trembitaMockServerEdr;
  @Inject
  @Qualifier("trembitaMockServerDracs")
  protected WireMockServer trembitaMockServerDracs;
  @Inject
  @Qualifier("trembitaMockServerIdpExchangeService")
  protected WireMockServer trembitaMockServerIdpExchangeService;
  @Value("${keycloak.citizen.realm}")
  protected String citizenRealm;
  @Value("${keycloak.officer.realm}")
  protected String officerRealm;
  @Value("${keycloak.officer-system-client.realm}")
  protected String officerSystemClientRealm;

  protected static String validAccessToken;

  @BeforeClass
  public static void setup() throws IOException {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    validAccessToken = new String(ByteStreams.toByteArray(Objects
        .requireNonNull(BaseIT.class.getResourceAsStream("/json/testuserAccessToken.json"))));
  }

  @Before
  public void setAuthorization() {
    cephService.clearStorage();
    runtimeService.createProcessInstanceQuery().list().forEach(
        processInstance -> runtimeService
            .deleteProcessInstance(processInstance.getId(), "test clear"));
    cephKeyProvider = new FormDataKeyProviderImpl();
  }

  protected void completeTask(String taskId, String processInstanceId, String formData) {
    var cephKey = cephKeyProvider.generateKey(taskId, processInstanceId);
    cephService.put(cephService.getCephBucketName(), cephKey, formData);
    String id = taskService.createTaskQuery().taskDefinitionKey(taskId).singleResult().getId();
    taskService.complete(id);
  }

  protected FormDataDto deserializeFormData(String formData) {
    try {
      return this.objectMapper.readValue(TestUtils.getContent(formData), FormDataDto.class);
    } catch (JsonProcessingException var4) {
      throw new IllegalStateException("Couldn't deserialize form data", var4);
    }
  }

  @SneakyThrows
  protected void stubSearchSubjects(String responseXmlFilePath) {
    stubTrembita(responseXmlFilePath, "SearchSubjects", trembitaMockServerEdr);
  }

  protected void stubSubjectDetail(String responseXmlFilePath) throws Exception {
    stubTrembita(responseXmlFilePath, "SubjectDetail", trembitaMockServerEdr);
  }

  @SneakyThrows
  protected void stubGetCertByNumRoleBirthDate(String responseXmlFilePath) {
    stubTrembita(responseXmlFilePath, "GetCertByNumRoleBirthDate", trembitaMockServerDracs);
  }

  @SneakyThrows
  protected void stubGetCertByNumRoleNames(String responseXmlFilePath) {
    stubTrembita(responseXmlFilePath, "GetCertByNumRoleNames", trembitaMockServerDracs);
  }

  protected void stubIDPexchangeService(String responseXmlFilePath) throws Exception {
    stubTrembita(responseXmlFilePath, "IDPexchangeService", trembitaMockServerIdpExchangeService);
  }

  protected void stubTrembita(String responseXmlFilePath, String serviceCode,
      WireMockServer server) throws Exception {
    String response = Files.readString(
        Paths.get(TestUtils.class.getResource(responseXmlFilePath).toURI()),
        StandardCharsets.UTF_8);

    server.addStubMapping(
        stubFor(post(urlPathEqualTo("/trembita-mock-server"))
            .withRequestBody(matching(String.format(".*%s.*", serviceCode)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody(response))));
  }

  protected void stubExcerptServiceRequest(StubData data) {
    var uriBuilder = Objects.nonNull(data.getUri()) ? data.getUri() :
        UriComponentsBuilder.fromPath(EXCERPT_SERVICE_MOCK_SERVER).pathSegment(data.getResource());
    excerptServiceWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  protected void stubSettingsRequest(StubData data) {
    var uriBuilder = UriComponentsBuilder.fromPath(SETTINGS_MOCK_SERVER)
        .pathSegment(data.getResource());

    userSettingsWireMock.addStubMapping(stubFor(getMappingBuilder(data, uriBuilder)));
  }

  @SneakyThrows
  protected String convertJsonToString(String jsonFilePath) {
    return TestUtils.getContent(jsonFilePath);
  }

  private MappingBuilder getMappingBuilder(StubData data, UriComponentsBuilder uriBuilder) {
    if (Objects.nonNull(data.getResourceId())) {
      uriBuilder.pathSegment(data.getResourceId());
    }

    var mappingBuilder = getMappingBuilderForMethod(data.getHttpMethod(),
        uriBuilder.encode().toUriString());
    data.getHeaders().forEach((key, value) -> mappingBuilder.withHeader(key, equalTo(value)));
    mappingBuilder.withHeader("Content-Type",
        equalTo(org.springframework.http.MediaType.APPLICATION_JSON_VALUE));

    data.getQueryParams()
        .forEach((key, value) -> mappingBuilder.withQueryParam(key, equalTo(value)));

    if (Objects.nonNull(data.getRequestBody())) {
      mappingBuilder.withRequestBody(equalToJson(TestUtils.getContent(data.getRequestBody())));
    }

    mappingBuilder
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
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

  protected void mockConnectToKeycloak(String realmName) {
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/auth/realms/" + realmName + "/protocol/openid-connect/token"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString("/json/keycloak/keycloakConnectResponse.json")))));
  }

  protected void mockKeycloakGetUserByUsername(String username, String realm, String responseBody) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(String.format("/auth/admin/realms/%s/users", realm)))
            .withQueryParam("username", equalTo(username))
            .withQueryParam("exact", equalTo(Boolean.TRUE.toString()))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(responseBody)))));
  }

  protected void mockGetKeycloakGetUserById(String userId, String realm, String responseBody) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo(String.format("/auth/admin/realms/%s/users/%s", realm, userId)))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(responseBody)))));
  }

  protected void mockKeycloakUpdateUser(String userId, String realm, String requestBody) {
    var roleMappingsUrl = String.format("/auth/admin/realms/%s/users/%s", realm, userId);
    keycloakMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo(roleMappingsUrl)).withRequestBody(
                equalToJson(convertJsonToString(requestBody)))
            .willReturn(aResponse().withStatus(200))));
  }

  protected void mockKeycloakGetUsersByAttributes(String realm, String requestBody, String responseBody) {
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(String.format("/auth/realms/%s/users/search", realm)))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString(responseBody)))));
  }
}
