package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.exception.KeycloakNotFoundException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

public class KeycloakConnectorDelegateIT extends BaseIT {

  @Inject
  @Qualifier("keycloakMockServer")
  private WireMockServer keycloakMockServer;
  private static String responseBodyRole;
  private static String responseBodyUsers;

  @BeforeClass
  public static void setUp() throws IOException {
    responseBodyRole = convertJsonToString("/json/keycloak/keycloakRoleResponse.json");
    responseBodyUsers = convertJsonToString("/json/keycloak/keycloakUsersResponse.json");
  }

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldAddRealmRoleToKeycloak() throws Exception {
    var roleMappingsUrl = "/auth/admin/realms/test-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    mockConnectToKeycloak();
    mockKeycloakGetUsers(responseBodyUsers);
    mockKeycloakGetRole(responseBodyRole, 200);
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo(roleMappingsUrl)).withRequestBody(equalTo(requestBodyRoles))));

    var processInstance = runtimeService.startProcessInstanceByKey("testAddRoleKeycloak_key", "");

    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalTo(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/testRemoveRoleKeycloak.bpmn"})
  public void shouldRemoveRealmRoleFromKeycloak() throws Exception {
    var roleMappingsUrl = "/auth/admin/realms/test-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    mockConnectToKeycloak();
    mockKeycloakGetUsers(responseBodyUsers);
    mockKeycloakGetRole(responseBodyRole, 200);
    keycloakMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo(roleMappingsUrl)).withRequestBody(equalTo(requestBodyRoles))));

    var processInstance = runtimeService.startProcessInstanceByKey("testRemoveKeycloakRoleKey", "");

    keycloakMockServer.verify(1,
        deleteRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalTo(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenRoleNotFound() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers(responseBodyUsers);
    mockKeycloakGetRole("", 404);

    var ex = assertThrows(KeycloakNotFoundException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Keycloak role not found!");
  }

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenUserNotFound() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers("[]");

    var ex = assertThrows(KeycloakNotFoundException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Keycloak user not found!");
  }

  private void mockConnectToKeycloak() throws IOException {
    keycloakMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(convertJsonToString("/json/keycloak/keycloakConnectResponse.json")))));
  }

  private void mockKeycloakGetUsers(String responseBody) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/test-realm/users"))
            .withQueryParam("username", equalTo("testuser"))
            .willReturn(aResponse().withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBody(responseBody))));
  }

  private void mockKeycloakGetRole(String responseBody, int status) {
    keycloakMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/auth/admin/realms/test-realm/roles/citizen"))
            .willReturn(aResponse().withStatus(status)
                .withHeader("Content-type", "application/json")
                .withBody(responseBody))));
  }

  private static String convertJsonToString(String jsonFilePath) throws IOException {
    return new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream(jsonFilePath)));
  }
}
