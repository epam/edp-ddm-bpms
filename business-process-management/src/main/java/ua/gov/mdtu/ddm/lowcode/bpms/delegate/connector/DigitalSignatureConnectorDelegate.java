package ua.gov.mdtu.ddm.lowcode.bpms.delegate.connector;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.dto.DataFactoryConnectorResponse;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used for digital
 * signature of data
 */
@Component("digitalSignatureConnectorDelegate")
@Logging
public class DigitalSignatureConnectorDelegate extends BaseConnectorDelegate {

  private final String dsoBaseUrl;

  @Autowired
  public DigitalSignatureConnectorDelegate(RestTemplate restTemplate,
      FormDataCephService formDataCephService,
      @Value("${spring.application.name}") String springAppName,
      @Value("${dso.url}") String dsoBaseUrl) {
    super(restTemplate, formDataCephService, springAppName);
    this.dsoBaseUrl = dsoBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);
    var response = performPost(execution, payload.toString());

    ((AbstractVariableScope) execution)
        .setVariableLocalTransient(RESPONSE_VARIABLE, response.getResponseBody());
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dsoBaseUrl).pathSegment("api", "eseal", "sign")
        .build().toUri();
    return perform(RequestEntity.post(uri).headers(getHeadersForSign(execution)).body(body));
  }

  private HttpHeaders getHeadersForSign(DelegateExecution execution) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    getAccessToken(execution).ifPresent(xAccessToken -> headers.add("X-Access-Token", xAccessToken));
    return headers;
  }
}
