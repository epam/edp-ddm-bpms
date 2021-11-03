package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used for digital
 * signature of data
 */
@Component(DigitalSignatureConnectorDelegate.DELEGATE_NAME)
public class DigitalSignatureConnectorDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "digitalSignatureConnectorDelegate";

  private final String dsoBaseUrl;

  @Autowired
  public DigitalSignatureConnectorDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${dso.url}") String dsoBaseUrl) {
    super(restTemplate, springAppName);
    this.dsoBaseUrl = dsoBaseUrl;
  }

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var payload = (SpinJsonNode) execution.getVariable(PAYLOAD_VARIABLE);

    logProcessExecution("sign data");
    var response = performPost(execution, payload.toString());

    setTransientResult(execution, RESPONSE_VARIABLE, response.getResponseBody());
    logDelegateExecution(execution, Set.of(PAYLOAD_VARIABLE), Set.of());
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution execution, String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(dsoBaseUrl).pathSegment("api", "eseal", "sign")
        .build().toUri();
    return perform(RequestEntity.post(uri).headers(getHeadersForSign(execution)).body(body));
  }

  private HttpHeaders getHeadersForSign(DelegateExecution execution) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    getAccessToken(execution)
        .ifPresent(xAccessToken -> headers.add("X-Access-Token", xAccessToken));
    return headers;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
