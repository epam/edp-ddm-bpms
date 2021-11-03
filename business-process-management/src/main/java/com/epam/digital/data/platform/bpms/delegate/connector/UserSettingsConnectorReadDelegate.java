package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(UserSettingsConnectorReadDelegate.DELEGATE_NAME)
public class UserSettingsConnectorReadDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "userSettingsConnectorReadDelegate";

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
    logStartDelegateExecution();
    logProcessExecution("read user settings  on resource", RESOURCE_SETTINGS);
    var response = performGet(execution);

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(), Set.of(RESPONSE_VARIABLE));
  }


  protected DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution) {
    var uri = UriComponentsBuilder.fromHttpUrl(userSettingsBaseUrl).pathSegment(RESOURCE_SETTINGS)
        .build().toUri();

    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
