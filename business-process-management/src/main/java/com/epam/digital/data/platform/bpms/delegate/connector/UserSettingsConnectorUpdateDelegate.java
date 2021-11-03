package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
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
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    logProcessExecution("create or update user settings on resource", RESOURCE_SETTINGS);
    var response = performPut(execution, payload.toString());

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(PAYLOAD_VARIABLE), Set.of(RESPONSE_VARIABLE));
  }

  private DataFactoryConnectorResponse performPut(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(userSettingsBaseUrl).pathSegment(RESOURCE_SETTINGS)
        .build().toUri();

    return perform(RequestEntity.put(uri).headers(getHeaders(execution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
