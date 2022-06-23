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
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RegistryConnectorResponse;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.spin.Spin;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public abstract class BaseRestTemplateConnectorDelegate extends BaseJavaDelegate {

  private static final String COMMA_REGEX = " *, *";

  private final RestTemplate restTemplate;

  protected URI buildUri(String url, String path, MultiValueMap<String, String> requestParameters) {
    return UriComponentsBuilder.fromHttpUrl(url)
        .pathSegment(path.split("/"))
        .queryParams(requestParameters).build().toUri();
  }

  protected MultiValueMap<String, String> toMultiValueMapSeparatedByComma(
      Map<String, String> map) {
    var result = new LinkedMultiValueMap<String, String>();
    if (CollectionUtils.isEmpty(map)) {
      return result;
    }
    map.forEach((name, value) -> result.put(name, splitParametersByComma(value)));
    return result;
  }

  protected List<String> splitParametersByComma(String parameterValue) {
    if (Objects.isNull(parameterValue)) {
      return List.of();
    }
    return Arrays.stream(parameterValue.split(COMMA_REGEX)).collect(Collectors.toList());
  }

  protected RegistryConnectorResponse sendRequest(URI uri, HttpMethod method,
      HttpEntity<String> httpEntity) {
    var response = restTemplate.exchange(uri, method, httpEntity, String.class);

    var responseBody =
        StringUtils.isBlank(response.getBody()) ? null : Spin.JSON(response.getBody());

    return RegistryConnectorResponse.builder()
        .statusCode(response.getStatusCode().value())
        .responseBody(responseBody)
        .build();
  }
}
