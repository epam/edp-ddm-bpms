package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to read user
 * settings.
 */
@Component("userSettingsConnectorReadDelegate")
public class UserSettingsConnectorReadDelegate extends BaseConnectorDelegate {

  private final String userSettingsBaseUrl;

  @Autowired
  public UserSettingsConnectorReadDelegate(
      RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${user-settings-service-api.url}") String userSettingsBaseUrl) {
    super(restTemplate, springAppName);
    this.userSettingsBaseUrl = userSettingsBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);

    DataFactoryConnectorResponse response = performGet(execution, resource);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }


  protected DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution,
      String resource) {
    var uri = UriComponentsBuilder.fromHttpUrl(userSettingsBaseUrl).pathSegment(resource).build()
        .toUri();

    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }
}
