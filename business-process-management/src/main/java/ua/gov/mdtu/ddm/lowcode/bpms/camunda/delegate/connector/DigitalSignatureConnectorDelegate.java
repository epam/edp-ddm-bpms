package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.DataFactoryConnectorResponse;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.service.MessageResolver;

@Component("digitalSignatureConnectorDelegate")
@Logging
public class DigitalSignatureConnectorDelegate extends BaseConnectorDelegate {

  private final String dsoBaseUrl;

  @Autowired
  public DigitalSignatureConnectorDelegate(RestTemplate restTemplate, CephService cephService,
      JacksonJsonParser jacksonJsonParser, MessageResolver messageResolver,
      @Value("${spring.application.name}") String springAppName,
      @Value("${ceph.bucket}") String cephBucketName,
      @Value("${dso.url}") String dsoBaseUrl) {
    super(restTemplate, cephService, jacksonJsonParser, messageResolver, springAppName, cephBucketName);
    this.dsoBaseUrl = dsoBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    var payload = (String) execution.getVariable("payload");
    var response = performPost(execution, payload);

    ((AbstractVariableScope) execution).setVariableLocalTransient("response", response.getResponseBody());
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dsoBaseUrl).pathSegment("api", "eseal", "sign").build().toUri();
    var requestEntity = RequestEntity.post(uri).headers(getHeadersForSign(execution)).body(body);
    try {
      return perform(requestEntity);
    } catch (RestClientResponseException ex) {
      throw buildUpdatableException(ex);
    }
  }

  private HttpHeaders getHeadersForSign(DelegateExecution execution) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    getAccessToken(execution).ifPresent(xAccessToken -> headers.add("X-Access-Token", xAccessToken));
    return headers;
  }
}
