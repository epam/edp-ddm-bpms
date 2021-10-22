package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.DataFactoryConnectorResponse;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(DataFactoryConnectorReadDelegate.DELEGATE_NAME)
public class DataFactoryConnectorReadDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorReadDelegate";

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
    logStartDelegateExecution();
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var id = (String) execution.getVariable(RESOURCE_ID_VARIABLE);

    logProcessExecution("get entity on resource", resource);
    var response = performGet(execution, resource, id);

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution, Set.of(RESOURCE_VARIABLE, RESOURCE_ID_VARIABLE),
        Set.of(RESOURCE_ID_VARIABLE));
  }

  protected DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution,
      String resourceName, String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
