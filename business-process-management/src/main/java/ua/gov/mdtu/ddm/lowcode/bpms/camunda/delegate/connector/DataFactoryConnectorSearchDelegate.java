package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

import java.util.Map;
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

@Component("dataFactoryConnectorSearchDelegate")
@Logging
public class DataFactoryConnectorSearchDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  @Autowired
  public DataFactoryConnectorSearchDelegate(RestTemplate restTemplate, CephService cephService,
      JacksonJsonParser jacksonJsonParser, MessageResolver messageResolver,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, cephService, jacksonJsonParser, messageResolver, springAppName,
        cephBucketName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) {
    var resource = (String) execution.getVariable("resource");
    var searchConditions = (Map<String, String>) execution.getVariable("searchConditions");

    var response = performSearch(execution, resource, searchConditions);

    ((AbstractVariableScope) execution).setVariableLocalTransient("response", response);
  }

  private DataFactoryConnectorResponse performSearch(DelegateExecution delegateExecution,
      String resourceName, Map<String, String> searchCriteria) {
    var uriBuilder = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName);
    if (searchCriteria != null) {
      searchCriteria.forEach(uriBuilder::queryParam);
    }

    var requestEntity = RequestEntity.get(uriBuilder.build().toUri())
        .headers(getHeaders(delegateExecution)).build();
    try {
      return perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildUpdatableException(ex);
    }
  }
}
