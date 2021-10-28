package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.Map;
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

  private final String dataFactoryBaseUrl;

  @SystemVariable(name = "searchConditions")
  private NamedVariableAccessor<Map<String, String>> searchConditionsVariable;

  @Autowired
  public DataFactoryConnectorSearchDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    logStartDelegateExecution();
    var resource = resourceVariable.from(execution).get();
    var searchConditions = searchConditionsVariable.from(execution).getOrDefault(Map.of());

    logProcessExecution("search entities on resource", resource);
    var response = performSearch(execution, resource, searchConditions);

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performSearch(DelegateExecution delegateExecution, String resourceName,
      Map<String, String> searchCriteria) {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .encode();
    searchCriteria.forEach(uriBuilder::queryParam);

    return perform(RequestEntity.get(uriBuilder.build().toUri())
        .headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return null;
  }
}
