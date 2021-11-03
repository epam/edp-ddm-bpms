package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
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
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var id = (String) execution.getVariable(RESOURCE_ID_VARIABLE);
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    logProcessExecution("update entity on resource", resource);
    var response = performPut(execution, resource, id, payload.toString());

    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution,
        Set.of(RESOURCE_VARIABLE, RESOURCE_ID_VARIABLE, PAYLOAD_VARIABLE),
        Set.of(RESPONSE_VARIABLE));
  }

  private DataFactoryConnectorResponse performPut(DelegateExecution delegateExecution,
      String resourceName, String resourceId, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    return perform(RequestEntity.put(uri).headers(getHeaders(delegateExecution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
