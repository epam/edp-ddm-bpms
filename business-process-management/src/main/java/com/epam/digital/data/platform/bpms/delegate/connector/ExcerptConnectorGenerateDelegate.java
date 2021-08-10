package com.epam.digital.data.platform.bpms.delegate.connector;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.bpms.delegate.dto.DataFactoryConnectorResponse;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
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
@Component
public class ExcerptConnectorGenerateDelegate extends BaseConnectorDelegate {

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
    var excerptType = (String) execution.getVariable(EXCERPT_TYPE_VAR);
    var excerptInputData = (Map<String, Object>) execution.getVariable(EXCERPT_INPUT_DATA_VAR);
    var requiresSystemSignature = Boolean.valueOf(
        (String) execution.getVariable(REQUIRES_SYSTEM_SIGNATURE_VAR));

    var requestBody = new ExcerptEventDto(null, excerptType, excerptInputData,
        requiresSystemSignature);

    var response = performPost(execution, objectMapper.writeValueAsString(requestBody));
    execution.setVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID,
        response.getResponseBody().prop("excerptIdentifier").value());
    ((AbstractVariableScope) execution).setVariableLocalTransient(RESPONSE_VARIABLE, response);
  }

  private DataFactoryConnectorResponse performPost(DelegateExecution delegateExecution,
      String body) {
    var uri = UriComponentsBuilder.fromHttpUrl(excerptServiceBaseUrl).pathSegment(RESOURCE_EXCERPTS)
        .build().toUri();
    return perform(RequestEntity.post(uri).headers(getHeaders(delegateExecution)).body(body));
  }
}
