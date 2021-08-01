package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to read data
 * from Data Factory
 */
@Component("dataFactoryConnectorReadDelegate")
@Logging
public class DataFactoryConnectorReadDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorReadDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var id = (String) execution.getVariable(RESOURCE_ID_VARIABLE);

    var response = performGet(execution, resource, id);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  protected DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution,
      String resourceName, String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }
}
