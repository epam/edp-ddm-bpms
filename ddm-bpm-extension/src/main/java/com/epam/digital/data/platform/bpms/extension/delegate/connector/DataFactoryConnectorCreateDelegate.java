package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(DataFactoryConnectorCreateDelegate.DELEGATE_NAME)
public class DataFactoryConnectorCreateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorCreateDelegate";

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorCreateDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var payload = payloadVariable.from(execution).getOptional();

    logProcessExecution("create entity on resource", resource);
    var response = performPost(execution, resource, payload.map(Objects::toString).orElse(null));

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performPost(DelegateExecution delegateExecution,
      String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    return perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
