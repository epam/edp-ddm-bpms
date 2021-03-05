package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

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
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.service.MessageResolver;

@Component("dataFactoryConnectorReadDelegate")
@Logging
public class DataFactoryConnectorReadDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorReadDelegate(RestTemplate restTemplate, CephService cephService,
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
    var id = (String) execution.getVariable(RESOURCE_ID_VARIABLE);

    var response = performGet(execution, resource, id);

    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performGet(DelegateExecution delegateExecution,
      String resourceName, String resourceId) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName)
        .pathSegment(resourceId).build().toUri();

    var requestEntity = RequestEntity.get(uri).headers(getHeaders(delegateExecution)).build();
    try {
      return perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildReadableException(requestEntity, ex);
    }
  }
}
