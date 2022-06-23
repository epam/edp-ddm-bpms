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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.rest;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to send request
 * to an external system
 */
@Slf4j
@Component(DataFactoryConnectorDelegate.DELEGATE_NAME)
public class DataFactoryConnectorDelegate extends BaseRestTemplateConnectorDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorDelegate";

  @SystemVariable(name = "methodParameter")
  private NamedVariableAccessor<String> methodParameterVariable;
  @SystemVariable(name = "path")
  private NamedVariableAccessor<String> pathVariable;
  @SystemVariable(name = "requestParameters")
  private NamedVariableAccessor<Map<String, String>> requestParametersVariable;
  @SystemVariable(name = "payload")
  private NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  private NamedVariableAccessor<RegistryConnectorResponse> responseVariable;

  private final HeaderBuilderFactory headerBuilderFactory;
  private final String registryRestApiUrl;

  public DataFactoryConnectorDelegate(RestTemplate restTemplate,
      HeaderBuilderFactory headerBuilderFactory,
      @Value("${registry-rest-api.url}") String registryRestApiUrl) {
    super(restTemplate);
    this.headerBuilderFactory = headerBuilderFactory;
    this.registryRestApiUrl = registryRestApiUrl;
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var methodString = methodParameterVariable.from(execution).getOrThrow();
    var path = pathVariable.from(execution).getOrThrow();

    var requestParameters =
        toMultiValueMapSeparatedByComma(requestParametersVariable.from(execution).get());
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();
    var payload = payloadVariable.from(execution).getOptional().map(Object::toString).orElse(null);

    var uri = buildUri(registryRestApiUrl, path, requestParameters);
    var method = HttpMethod.valueOf(methodString.toUpperCase());
    var httpEntity = new HttpEntity<>(payload, headers);

    var responseValue = sendRequest(uri, method, httpEntity);

    responseVariable.on(execution).set(responseValue);
  }
}
