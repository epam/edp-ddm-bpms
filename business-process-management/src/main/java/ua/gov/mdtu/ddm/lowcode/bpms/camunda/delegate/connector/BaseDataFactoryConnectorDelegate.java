package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.connector;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.dto.DataFactoryConnectorResponse;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseDataFactoryConnectorDelegate implements JavaDelegate {

  private final RestTemplate restTemplate;
  private final CephService cephService;
  private final JacksonJsonParser jacksonJsonParser;
  private final String springAppName;
  private final String cephBucketName;

  protected DataFactoryConnectorResponse perform(RequestEntity<?> requestEntity) {
    try {
      var httpResponse = restTemplate.exchange(requestEntity, String.class);

      log.info("Successfully sent {} request to {}", requestEntity.getMethod(),
          requestEntity.getUrl());

      return DataFactoryConnectorResponse.builder()
          .statusCode(httpResponse.getStatusCode().value())
          .responseBody(httpResponse.getBody())
          .headers(httpResponse.getHeaders())
          .build();
    } catch (RestClientResponseException ex) {
      log.info("Sent {} request to {} with result status {}, message - {}",
          requestEntity.getMethod(),
          requestEntity.getUrl(), ex.getRawStatusCode(), ex.getMessage(), ex);
      return DataFactoryConnectorResponse.builder()
          .statusCode(ex.getRawStatusCode())
          .headers(ex.getResponseHeaders())
          .responseBody(ex.getResponseBodyAsString())
          .build();
    }
  }

  @SuppressWarnings("unchecked")
  protected HttpHeaders getHeaders(DelegateExecution delegateExecution) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Source-System", "Low-code Platform");
    headers.add("X-Source-Application", springAppName);
    headers.add("X-Source-Business-Process",
        ((ExecutionEntity) delegateExecution).getProcessDefinition().getName());
    headers.add("X-Source-Business-Activity",
        delegateExecution.getActivityInstanceId());

    getAccessToken(delegateExecution).ifPresent(xAccessToken ->
        headers.add("X-Access-Token", xAccessToken));
    getCephKey(delegateExecution, "x_digital_signature_var").ifPresent(cephKey ->
        headers.add("X-Digital-Signature", cephKey));
    getCephKey(delegateExecution, "x_digital_signature_derived_var").ifPresent(cephKey ->
        headers.add("X-Digital-Signature-Derived", cephKey));

    var customHeaders = (Map<String, String>) delegateExecution.getVariable("headers");
    if (customHeaders != null) {
      customHeaders.forEach(headers::add);
    }

    return headers;
  }

  private Optional<String> getCephKey(DelegateExecution delegateExecution, String localVarName) {
    var cephKeyVar = (String) delegateExecution.getVariable(localVarName);
    return StringUtils.isBlank(cephKeyVar) ? Optional.empty()
        : Optional.ofNullable((String) delegateExecution.getVariable(cephKeyVar));
  }

  private Optional<String> getAccessToken(DelegateExecution delegateExecution) {
    var xAccessTokenCephKey = getCephKey(delegateExecution, "x_access_token_var");
    if (xAccessTokenCephKey.isEmpty()) {
      return Optional.empty();
    }
    var xAccessTokenCephDoc = cephService.getContent(cephBucketName, xAccessTokenCephKey.get());

    Map<String, Object> map = jacksonJsonParser.parseMap(xAccessTokenCephDoc);
    return Optional.ofNullable(map.get("x-access-token")).map(Object::toString);
  }
}
