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
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to delete
 * data in Data Factory
 */
@Component(DataFactoryConnectorDeleteDelegate.DELEGATE_NAME)
public class DataFactoryConnectorDeleteDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorDeleteDelegate";

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorDeleteDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var id = (String) execution.getVariable(RESOURCE_ID_VARIABLE);

    var response = performDelete(execution, resource, id);

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(RESOURCE_VARIABLE, RESOURCE_ID_VARIABLE),
        Set.of(RESPONSE_VARIABLE));
  }

  private DataFactoryConnectorResponse performDelete(DelegateExecution delegateExecution,
      String resourceName, String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.delete(uri).headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
