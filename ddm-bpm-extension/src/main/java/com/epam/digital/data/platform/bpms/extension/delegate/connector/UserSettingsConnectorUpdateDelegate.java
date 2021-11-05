package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to create or
 * update user settings.
 */
@Slf4j
@Component(UserSettingsConnectorUpdateDelegate.DELEGATE_NAME)
public class UserSettingsConnectorUpdateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "userSettingsConnectorUpdateDelegate";

  private final String userSettingsBaseUrl;

  @Autowired
  public UserSettingsConnectorUpdateDelegate(
      RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${user-settings-service-api.url}") String userSettingsBaseUrl) {
    super(restTemplate, springAppName);
    this.userSettingsBaseUrl = userSettingsBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var payload = payloadVariable.from(execution).getOptional();

    log.debug("Start creating or updating user settings on resource {}", RESOURCE_SETTINGS);
    var response = performPut(execution, payload.map(Object::toString).orElse(null));
    log.debug("User settings successfully created or updated");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performPut(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(userSettingsBaseUrl).pathSegment(RESOURCE_SETTINGS)
        .build().toUri();

    return perform(RequestEntity.put(uri).headers(getHeaders(execution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
