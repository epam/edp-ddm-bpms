package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used for getting
 * excerpt status
 */
@Component
public class ExcerptConnectorStatusDelegate extends BaseConnectorDelegate {

  public static final String EXCERPT_ID_VAR = "excerptIdentifier";

  private final String excerptServiceBaseUrl;

  @Autowired
  public ExcerptConnectorStatusDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${excerpt-service-api.url}") String excerptServiceBaseUrl,
      ObjectMapper objectMapper) {
    super(restTemplate, springAppName);
    this.excerptServiceBaseUrl = excerptServiceBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var excerptIdentifier = (String) execution.getVariable(EXCERPT_ID_VAR);

    var response = performGet(execution, excerptIdentifier);
    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  protected DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution,
      String id) {
    var uri = UriComponentsBuilder.fromHttpUrl(excerptServiceBaseUrl).pathSegment(RESOURCE_EXCERPTS)
        .pathSegment(id).pathSegment("status").build().toUri();
    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }
}
