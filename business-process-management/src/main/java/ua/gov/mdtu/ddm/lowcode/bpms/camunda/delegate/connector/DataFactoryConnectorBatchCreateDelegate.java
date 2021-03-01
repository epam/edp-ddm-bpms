package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.service.MessageResolver;

@Component("dataFactoryConnectorBatchCreateDelegate")
@Logging
@Slf4j
public class DataFactoryConnectorBatchCreateDelegate extends BaseConnectorDelegate {

  private final String dataFactoryBaseUrl;

  public DataFactoryConnectorBatchCreateDelegate(RestTemplate restTemplate, CephService cephService,
      JacksonJsonParser jacksonJsonParser, MessageResolver messageResolver,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, cephService, jacksonJsonParser, messageResolver, springAppName,
        cephBucketName);
    this.dataFactoryBaseUrl = dataFactoryBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var resource = (String) execution.getVariable("resource");
    var payload = (SpinJsonNode) execution.getVariable("payload");

    var response = executeBatchCreateOperation(execution, payload, resource);

    ((AbstractVariableScope) execution).setVariableLocalTransient("response", response);
  }

  private DataFactoryConnectorResponse executeBatchCreateOperation(
      DelegateExecution execution,
      SpinJsonNode payload, String resource) {

    for (SpinJsonNode spinJsonNode : payload.elements()) {
      var stringJsonNode = spinJsonNode.toString();
      log.debug("Post request to data factory, resource: {}, body: {}", resource, stringJsonNode);
      var response = performPost(execution, resource, stringJsonNode);
      log.debug("Response from data factory, code: {}, body: {}, headers: {}",
          response.getStatusCode(), response.getResponseBody(), response.getHeaders());
    }
    return DataFactoryConnectorResponse.builder()
        .statusCode(HttpStatus.CREATED.value())
        .build();
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution delegateExecution,
      String resourceName, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dataFactoryBaseUrl).pathSegment(resourceName).build()
        .toUri();

    var requestEntity = RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body);
    try {
      return perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildUpdatableException(ex);
    }
  }
}
