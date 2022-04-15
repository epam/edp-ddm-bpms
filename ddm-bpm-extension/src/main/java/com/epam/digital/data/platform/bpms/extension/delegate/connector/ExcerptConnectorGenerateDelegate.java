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

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.excerpt.client.ExcerptFeignClient;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for excerpt
 * generation
 */
@Slf4j
@RequiredArgsConstructor
@Component(ExcerptConnectorGenerateDelegate.DELEGATE_NAME)
public class ExcerptConnectorGenerateDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "excerptConnectorGenerateDelegate";


  @SystemVariable(name = "excerptType")
  private NamedVariableAccessor<String> excerptTypeVariable;
  @SystemVariable(name = "excerptInputData")
  private NamedVariableAccessor<Map<String, Object>> excerptInputDataVariable;
  @SystemVariable(name = "requiresSystemSignature")
  private NamedVariableAccessor<String> requiresSystemSignatureVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  private final ExcerptFeignClient excerptFeignClient;
  private final ObjectMapper objectMapper;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var excerptType = excerptTypeVariable.from(execution).get();
    var excerptInputData = excerptInputDataVariable.from(execution).getOrDefault(Map.of());
    var requiresSystemSignature = Boolean.parseBoolean(
        requiresSystemSignatureVariable.from(execution).get());

    var requestBody = new ExcerptEventDto(null, excerptType, excerptInputData,
        requiresSystemSignature);

    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();
    log.debug("Start generating excerpt");
    var response = excerptFeignClient
        .performPost(objectMapper.writeValueAsString(requestBody), headers);
    log.debug("Excerpt successfully generated");

    responseVariable.on(execution).set(response);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
