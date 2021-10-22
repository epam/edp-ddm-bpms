package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.DataFactoryConnectorResponse;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(DataFactoryConnectorSearchDelegate.DELEGATE_NAME)
public class DataFactoryConnectorSearchDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorSearchDelegate";
  private static final String SEARCH_CONDITIONS_VARIABLE = "searchConditions";

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
    logStartDelegateExecution();
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var searchConditions = (Map<String, String>) execution.getVariable(SEARCH_CONDITIONS_VARIABLE);

    logProcessExecution("search entities on resource", resource);
    var response = performSearch(execution, resource, searchConditions);

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(RESOURCE_VARIABLE, SEARCH_CONDITIONS_VARIABLE),
        Set.of(RESPONSE_VARIABLE));
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

  @Override
  public String getDelegateName() {
    return null;
  }
}
