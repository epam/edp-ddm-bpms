package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
@Component(ExcerptConnectorStatusDelegate.DELEGATE_NAME)
public class ExcerptConnectorStatusDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "excerptConnectorStatusDelegate";

  private final String excerptServiceBaseUrl;

  @SystemVariable(name = "excerptIdentifier")
  private NamedVariableAccessor<String> excerptIdentifierVariable;

  @Autowired
  public ExcerptConnectorStatusDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${excerpt-service-api.url}") String excerptServiceBaseUrl) {
    super(restTemplate, springAppName);
    this.excerptServiceBaseUrl = excerptServiceBaseUrl;
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var excerptIdentifier = excerptIdentifierVariable.from(execution).get();

    logProcessExecution("get excerpt status on resource", RESOURCE_EXCERPTS);
    var response = performGet(execution, excerptIdentifier);
    responseVariable.on(execution).set(response);
  }

  protected ConnectorResponse performGet(DelegateExecution delegateExecution, String id) {
    var uri = UriComponentsBuilder.fromHttpUrl(excerptServiceBaseUrl).pathSegment(RESOURCE_EXCERPTS)
        .pathSegment(id).pathSegment("status").build().toUri();
    return perform(RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
