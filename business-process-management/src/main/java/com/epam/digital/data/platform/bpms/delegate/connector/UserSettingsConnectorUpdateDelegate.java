package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
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
@Component("userSettingsConnectorUpdateDelegate")
public class UserSettingsConnectorUpdateDelegate extends BaseConnectorDelegate {

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
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    var response = performPut(execution, resource, payload.toString());

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performPut(DelegateExecution delegateExecution,
      String resource, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(userSettingsBaseUrl).pathSegment(resource).build()
        .toUri();

    return perform(RequestEntity.put(uri).headers(getHeaders(delegateExecution)).body(body));
  }
}
