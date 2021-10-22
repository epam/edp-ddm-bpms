package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseConnectorDelegate} that is used for excerpt
 * generation
 */
@Component(ExcerptConnectorGenerateDelegate.DELEGATE_NAME)
public class ExcerptConnectorGenerateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "excerptConnectorGenerateDelegate";

  public static final String EXCERPT_TYPE_VAR = "excerptType";
  public static final String EXCERPT_INPUT_DATA_VAR = "excerptInputData";
  public static final String REQUIRES_SYSTEM_SIGNATURE_VAR = "requiresSystemSignature";

  private final String excerptServiceBaseUrl;
  private final ObjectMapper objectMapper;

  @Autowired
  public ExcerptConnectorGenerateDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${excerpt-service-api.url}") String excerptServiceBaseUrl,
      ObjectMapper objectMapper) {
    super(restTemplate, springAppName);
    this.excerptServiceBaseUrl = excerptServiceBaseUrl;
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) throws Exception {
    logStartDelegateExecution();
    var excerptType = (String) execution.getVariable(EXCERPT_TYPE_VAR);
    var excerptInputData = (Map<String, Object>) execution.getVariable(EXCERPT_INPUT_DATA_VAR);
    var requiresSystemSignature = Boolean.parseBoolean(
        (String) execution.getVariable(REQUIRES_SYSTEM_SIGNATURE_VAR));

    var requestBody = new ExcerptEventDto(null, excerptType, excerptInputData,
        requiresSystemSignature);

    logProcessExecution("generate excerpt on resource", RESOURCE_EXCERPTS);
    var response = performPost(execution, objectMapper.writeValueAsString(requestBody));
    setTransientResult(execution, RESPONSE_VARIABLE, response);
    logDelegateExecution(execution,
        Set.of(EXCERPT_TYPE_VAR, EXCERPT_INPUT_DATA_VAR, REQUIRES_SYSTEM_SIGNATURE_VAR),
        Set.of(RESPONSE_VARIABLE));
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution delegateExecution,
      String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(excerptServiceBaseUrl).pathSegment(RESOURCE_EXCERPTS)
        .build().toUri();
    return perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
