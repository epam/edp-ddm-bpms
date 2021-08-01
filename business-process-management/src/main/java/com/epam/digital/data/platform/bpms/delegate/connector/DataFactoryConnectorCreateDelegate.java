package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
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
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to create
 * data in Data Factory
 */
@Component("dataFactoryConnectorCreateDelegate")
@Logging
public class DataFactoryConnectorCreateDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorCreateDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    var response = performPost(execution, resource, payload.toString());

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution delegateExecution,
      String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    return perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }
}
