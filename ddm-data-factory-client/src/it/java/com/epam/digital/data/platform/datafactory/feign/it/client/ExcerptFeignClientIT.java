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

package com.epam.digital.data.platform.datafactory.feign.it.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.datafactory.feign.client.ExcerptFeignClient;
import com.epam.digital.data.platform.datafactory.feign.it.builder.StubRequest;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

class ExcerptFeignClientIT extends BaseIT {

  @Autowired
  private ExcerptFeignClient excerptFeignClient;

  @Test
  void shouldPerformGet() {
    var id = "testId";
    var expectedBody = "{\"status\": \"IN_PROGRESS\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockExcerptFeignClient(StubRequest.builder()
        .path(String.format("/excerpts/%s/status", id))
        .method(HttpMethod.GET)
        .requestHeaders(headers)
        .status(200)
        .responseBody(expectedBody)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var response = excerptFeignClient.performGet(id, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().prop("status").value()).isEqualTo("IN_PROGRESS");
  }

  @Test
  void shouldPerformPost() {
    var requestBody = "{\"recordId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"requiresSystemSignature\": true}";
    var responseBody = "{\"excerptIdentifier\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockExcerptFeignClient(StubRequest.builder()
        .path("/excerpts")
        .method(HttpMethod.POST)
        .requestHeaders(headers)
        .requestBody(equalTo(requestBody))
        .status(200)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody(responseBody)
        .build());

    var response = excerptFeignClient.performPost(requestBody, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().prop("excerptIdentifier").value())
        .isEqualTo("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  }

  @Test
  void shouldThrowSystemExceptionWhenPerformPost() {
    var requestBody = "{\"recordId\":}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockExcerptFeignClient(StubRequest.builder()
        .path("/excerpts")
        .method(HttpMethod.POST)
        .requestHeaders(headers)
        .requestBody(equalTo(requestBody))
        .status(500)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody("{\"traceId\":\"traceId1\",\"code\":\"Internal error\"}")
        .build());

    var ex = assertThrows(SystemException.class,
        () -> excerptFeignClient.performPost(requestBody, headers));

    assertThat(ex).isNotNull();
    assertThat(ex.getCode()).isEqualTo("Internal error");
  }
}
