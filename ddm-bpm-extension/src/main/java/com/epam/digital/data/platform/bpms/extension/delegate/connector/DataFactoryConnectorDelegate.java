/*
 * Copyright 2022 EPAM Systems.
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
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to send request
 * to an external system
 */
@Slf4j
@RequiredArgsConstructor
@Component(DataFactoryConnectorDelegate.DELEGATE_NAME)
public class DataFactoryConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorDelegate";

  private static final String COMMA_REGEX = " *, *";

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

  private final RestTemplate restTemplate;
  private final HeaderBuilderFactory headerBuilderFactory;
  @Value("${registry-rest-api.url}")
  private final String registryRestApiUrl;

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

    var uri = buildUri(path, requestParameters);
    var method = HttpMethod.valueOf(methodString.toUpperCase());
    var httpEntity = new HttpEntity<>(payload, headers);

    var response = restTemplate.exchange(uri, method, httpEntity, String.class);

    var responseBody =
        StringUtils.isBlank(response.getBody()) ? null : Spin.JSON(response.getBody());

    var responseValue = RegistryConnectorResponse.builder()
        .statusCode(response.getStatusCode().value())
        .responseBody(responseBody)
        .build();

    responseVariable.on(execution).set(responseValue);
  }


  private URI buildUri(String path, MultiValueMap<String, String> requestParameters) {
    return UriComponentsBuilder.fromHttpUrl(registryRestApiUrl)
        .pathSegment(path.split("/"))
        .queryParams(requestParameters).build().toUri();
  }

  private MultiValueMap<String, String> toMultiValueMapSeparatedByComma(
      Map<String, String> map) {
    var result = new LinkedMultiValueMap<String, String>();
    if (CollectionUtils.isEmpty(map)) {
      return result;
    }
    map.forEach((name, value) -> result.put(name, splitParametersByComma(value)));
    return result;
  }

  private List<String> splitParametersByComma(String parameterValue) {
    if (Objects.isNull(parameterValue)) {
      return List.of();
    }
    return Arrays.stream(parameterValue.split(COMMA_REGEX)).collect(Collectors.toList());
  }
}
