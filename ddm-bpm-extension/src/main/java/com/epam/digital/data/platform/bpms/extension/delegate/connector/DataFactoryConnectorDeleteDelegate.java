package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var id = resourceIdVariable.from(execution).get();

    log.debug("Start deleting entity by id {} on resource {}", id, resource);
    var response = performDelete(execution, resource, id);
    log.debug("Entity successfully deleted");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performDelete(DelegateExecution delegateExecution, String resourceName,
      String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.delete(uri).headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
