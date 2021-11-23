/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component(ExcerptConnectorGenerateDelegate.DELEGATE_NAME)
public class ExcerptConnectorGenerateDelegate extends BaseConnectorDelegate {

  public static final String DELEGATE_NAME = "excerptConnectorGenerateDelegate";

  private final String excerptServiceBaseUrl;
  private final ObjectMapper objectMapper;

  @SystemVariable(name = "excerptType")
  private NamedVariableAccessor<String> excerptTypeVariable;
  @SystemVariable(name = "excerptInputData")
  private NamedVariableAccessor<Map<String, Object>> excerptInputDataVariable;
  @SystemVariable(name = "requiresSystemSignature")
  private NamedVariableAccessor<String> requiresSystemSignatureVariable;

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
  public void executeInternal(DelegateExecution execution) throws Exception {
    var excerptType = excerptTypeVariable.from(execution).get();
    var excerptInputData = excerptInputDataVariable.from(execution).getOrDefault(Map.of());
    var requiresSystemSignature = Boolean.parseBoolean(
        requiresSystemSignatureVariable.from(execution).get());

    var requestBody = new ExcerptEventDto(null, excerptType, excerptInputData,
        requiresSystemSignature);

    log.debug("Start generating excerpt on resource {}", RESOURCE_EXCERPTS);
    var response = performPost(execution, objectMapper.writeValueAsString(requestBody));
    log.debug("Excerpt successfully generated");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse performPost(DelegateExecution delegateExecution,
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
