package com.epam.digital.data.platform.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.epam.digital.data.platform.bpms.exception.KeycloakNotFoundException;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class KeycloakConnectorDelegateIT extends BaseIT {

  private static final String RESPONSE_BODY_ROLE = "/json/keycloak/keycloakRoleResponse.json";
  private static final String RESPONSE_BODY_USERS = "/json/keycloak/keycloakUsersResponse.json";

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldAddRealmRoleToKeycloak() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers("testuser", RESPONSE_BODY_USERS);
    mockKeycloakGetRole("citizen", RESPONSE_BODY_ROLE, 200);
    mockKeycloakAddRole("7004ebde-68cf-4e25-bb76-b1642a3814e5",
        "/json/keycloak/keycloakRequestBodyRoles.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testAddRoleKeycloak_key", "");

    var roleMappingsUrl = "/auth/admin/realms/test-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    keycloakMockServer.verify(1,
        postRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/testRemoveRoleKeycloak.bpmn"})
  public void shouldRemoveRealmRoleFromKeycloak() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers("testuser", RESPONSE_BODY_USERS);
    mockKeycloakGetRole("citizen", RESPONSE_BODY_ROLE, 200);
    mockKeycloakDeleteRole("7004ebde-68cf-4e25-bb76-b1642a3814e5",
        "/json/keycloak/keycloakRequestBodyRoles.json");

    var processInstance = runtimeService.startProcessInstanceByKey("testRemoveKeycloakRoleKey", "");

    var roleMappingsUrl = "/auth/admin/realms/test-realm/users/7004ebde-68cf-4e25-bb76-b1642a3814e5/role-mappings/realm";
    var requestBodyRoles = convertJsonToString("/json/keycloak/keycloakRequestBodyRoles.json");
    keycloakMockServer.verify(1,
        deleteRequestedFor(urlEqualTo(roleMappingsUrl)).withRequestBody(equalToJson(requestBodyRoles)));

    BpmnAwareTests.assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenRoleNotFound() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers("testuser", RESPONSE_BODY_USERS);
    mockKeycloakGetRole("citizen", "", 404);

    var ex = assertThrows(KeycloakNotFoundException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Keycloak role not found!");
  }

  @Test
  @Deployment(resources = {"bpmn/testAddRoleKeycloak.bpmn"})
  public void shouldGetExceptionWhenUserNotFound() throws Exception {
    mockConnectToKeycloak();
    mockKeycloakGetUsers("testuser", "[]");

    var ex = assertThrows(KeycloakNotFoundException.class, () -> runtimeService
        .startProcessInstanceByKey("testAddRoleKeycloak_key"));

    assertThat(ex).isNotNull();
    assertThat(ex.getMessage()).isEqualTo("Keycloak user not found!");
  }
}
