package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.service.MessageResolver;

@Component("dataFactoryConnectorCreateDelegate")
@Logging
public class DataFactoryConnectorCreateDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorCreateDelegate(RestTemplate restTemplate, CephService cephService,
      JacksonJsonParser jacksonJsonParser, MessageResolver messageResolver,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, cephService, jacksonJsonParser, messageResolver, springAppName,
        cephBucketName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable(RESOURCE_VARIABLE);
    var payload = (String) execution.getVariable(PAYLOAD_VARIABLE);

    var response = performPost(execution, resource, payload);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution delegateExecution,
      String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    var requestEntity = RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body);
    try {
      return perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildUpdatableException(requestEntity, ex);
    }
  }
}
