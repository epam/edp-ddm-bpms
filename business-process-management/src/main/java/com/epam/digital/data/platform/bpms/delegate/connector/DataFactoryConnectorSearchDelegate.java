package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to search
 * data in Data Factory
 */
@Component("dataFactoryConnectorSearchDelegate")
@Logging
public class DataFactoryConnectorSearchDelegate extends BaseConnectorDelegate {

  protected static final String SEARCH_CONDITIONS_VARIABLE = "searchConditions";
  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorSearchDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var searchConditions = (Map<String, String>) execution.getVariable(SEARCH_CONDITIONS_VARIABLE);

    var response = performSearch(execution, resource, searchConditions);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performSearch(DelegateExecution delegateExecution,
      String resourceName, Map<String, String> searchCriteria) {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .encode();
    if (searchCriteria != null) {
      searchCriteria.forEach(uriBuilder::queryParam);
    }

    return perform(RequestEntity.get(uriBuilder.build().toUri())
        .headers(getHeaders(delegateExecution)).build());
  }
}
