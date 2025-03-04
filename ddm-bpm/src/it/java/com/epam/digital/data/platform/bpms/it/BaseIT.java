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

package com.epam.digital.data.platform.bpms.it;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;

import com.epam.digital.data.platform.bpms.it.util.TestUtils;
import com.epam.digital.data.platform.starter.security.SystemRole;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(count = 3)
public abstract class BaseIT {

  protected final String TOKEN_HEADER = "x-access-token";

  @Inject
  protected RuntimeService runtimeService;
  @Inject
  protected TaskService taskService;
  @Inject
  protected FormDataStorageService<?> formDataStorageService;
  @Inject
  private ObjectMapper objectMapper;
  @Inject
  private AuthorizationService authorizationService;
  @Inject
  private IdentityService identityService;

  @Inject
  @Qualifier("keycloakMockServer")
  protected WireMockServer keycloakMockServer;

  @LocalServerPort
  protected int port;
  protected Client jerseyClient = JerseyClientBuilder.createClient();

  protected static String validAccessToken;

  @BeforeClass
  public static void setup() throws IOException {
    validAccessToken = new String(ByteStreams.toByteArray(Objects
        .requireNonNull(BaseIT.class.getResourceAsStream("/json/testuserAccessToken.json"))));
  }

  @Before
  public void setAuthorization() {
    SecurityContextHolder.getContext().setAuthentication(null);
    Stream.of(SystemRole.getRoleNames()).forEach(this::createAuthorizationsIfNotExists);

    identityService.setAuthentication("testUser", List.of("camunda-admin"));
  }

  protected <T> T postForObject(String url, String body, Class<T> targetClass)
      throws JsonProcessingException {
    return this.postForObject(url, body, targetClass, validAccessToken);
  }

  protected <T> T getForObject(String url, Class<T> targetClass, String accessToken)
      throws IOException {
    String jsonResponse = jerseyClient
        .target(String.format("http://localhost:%d/%s", port, url))
        .request(MediaType.APPLICATION_JSON)
        .header(TOKEN_HEADER, accessToken)
        .get().readEntity(String.class);
    return objectMapper.readValue(jsonResponse, targetClass);
  }

  protected <T> T postForObject(String url, String body, Class<T> targetClass, String accessToken)
      throws JsonProcessingException {
    String jsonResponse = jerseyClient.target(String.format("http://localhost:%d/%s", port, url))
        .request()
        .header(TOKEN_HEADER, accessToken)
        .post(Entity.entity(body, MediaType.APPLICATION_JSON))
        .readEntity(String.class);
    return objectMapper.readValue(jsonResponse, targetClass);
  }

  private void createAuthorizationsIfNotExists(String groupId) {
    if (CollectionUtils.isEmpty(
        authorizationService.createAuthorizationQuery().groupIdIn(groupId).list())) {
      createAuthorization(Resources.PROCESS_DEFINITION,
          new Permission[]{ProcessDefinitionPermissions.CREATE_INSTANCE,
              ProcessDefinitionPermissions.READ}, groupId);
      createAuthorization(Resources.PROCESS_INSTANCE, new Permission[]{Permissions.CREATE},
          groupId);
    }
  }

  private void createAuthorization(Resources resources, Permission[] permissions, String groupId) {
    var authorization = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
    authorization.setResource(resources);
    authorization.setResourceId("*");
    authorization.setPermissions(permissions);
    authorization.setUserId(null);
    authorization.setGroupId(groupId);
    authorizationService.saveAuthorization(authorization);
  }

  @SneakyThrows
  protected String convertJsonToString(String jsonFilePath) {
    return TestUtils.getContent(jsonFilePath);
  }
}
