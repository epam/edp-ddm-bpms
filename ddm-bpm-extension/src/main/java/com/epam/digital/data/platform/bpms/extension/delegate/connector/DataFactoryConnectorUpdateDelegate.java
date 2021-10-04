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
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used to update
 * data in Data Factory
 */
@Slf4j
@Component("dataFactoryConnectorUpdateDelegate")
public class DataFactoryConnectorUpdateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorUpdateDelegate";

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorUpdateDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var id = resourceIdVariable.from(execution).get();
    var payload = payloadVariable.from(execution).getOptional();

    log.debug("Start updating entity with id {} on resource {}", id, resource);
    var response = performPut(execution, resource, id, payload.map(Object::toString).orElse(null));
    log.debug("Entity with id {} successfully updated", id);

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performPut(DelegateExecution delegateExecution, String resourceName,
      String resourceId, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.put(uri).headers(getHeaders(delegateExecution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}