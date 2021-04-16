package com.epam.digital.data.platform.bpms.it;

import com.epam.digital.data.platform.bpms.it.config.TestCephServiceImpl;
import com.epam.digital.data.platform.bpms.it.config.TestFormDataCephServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  protected final String TOKEN_HEADER = "x-access-token";

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
  protected TestFormDataCephServiceImpl formDataCephService;

  @LocalServerPort
  protected int port;
  protected Client jerseyClient = JerseyClientBuilder.createClient();

  protected static String validAccessToken;

  @BeforeClass
  public static void setup() throws IOException {
    validAccessToken = new String(ByteStreams
        .toByteArray(BaseIT.class.getResourceAsStream("/json/testuserAccessToken.json")));
  }

  protected <T> T getForObject(String url, Class<T> targetClass) throws IOException {
    return this.getForObject(url, targetClass, validAccessToken);
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
}
