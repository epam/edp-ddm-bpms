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

import com.epam.digital.data.platform.datafactory.feign.client.DataFactoryFeignClient;
import com.epam.digital.data.platform.datafactory.feign.it.builder.StubRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

class DataFactoryFeignClientIT extends BaseIT {

  @Autowired
  private DataFactoryFeignClient dataFactoryFeignClient;

  @Test
  void shouldPerformGet() {
    var resource = "testResource";
    var id = "testId";
    var expectedBody = "{\"testGet\": \"dataToRead\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockDataFactoryFeignClient(StubRequest.builder()
        .path(String.format("/%s/%s", resource, id))
        .method(HttpMethod.GET)
        .requestHeaders(headers)
        .status(200)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody(expectedBody)
        .build());

    var response = dataFactoryFeignClient.performGet(resource, id, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().prop("testGet").value()).isEqualTo("dataToRead");
  }

  @Test
  void shouldPerformPost() {
    var resource = "testResource";
    var expectedBody = "{\"testPost\": \"dataToCreate\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockDataFactoryFeignClient(StubRequest.builder()
        .path(String.format("/%s", resource))
        .method(HttpMethod.POST)
        .requestHeaders(headers)
        .requestBody(equalTo(expectedBody))
        .status(201)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var response = dataFactoryFeignClient.performPost(resource, expectedBody, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody()).isNull();
    assertThat(response.getStatusCode()).isEqualTo(201);
  }

  @Test
  void shouldPerformPut() {
    var resource = "testResource";
    var id = "testId";
    var expectedBody = "{\"testPut\": \"dataToUpdate\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockDataFactoryFeignClient(StubRequest.builder()
        .path(String.format("/%s/%s", resource, id))
        .method(HttpMethod.PUT)
        .requestHeaders(headers)
        .requestBody(equalTo(expectedBody))
        .status(204)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var response = dataFactoryFeignClient.performPut(resource, id, expectedBody, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody()).isNull();
  }

  @Test
  void shouldPerformDelete() {
    var resource = "testResource";
    var id = "testId";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockDataFactoryFeignClient(StubRequest.builder()
        .path(String.format("/%s/%s", resource, id))
        .method(HttpMethod.DELETE)
        .requestHeaders(headers)
        .status(204)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .build());

    var response = dataFactoryFeignClient.performDelete(resource, id, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody()).isNull();
    assertThat(response.getStatusCode()).isEqualTo(204);
  }

  @Test
  void shouldPerformSearch() {
    var resource = "testResource";
    var expectedBody = "[{\"testGet\": \"dataToSearch\"}]";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");
    var queryParams = Map.of("id", "testId", "name", "testName");

    mockDataFactoryFeignClient(StubRequest.builder()
        .path(String.format("/%s", resource))
        .method(HttpMethod.GET)
        .requestHeaders(headers)
        .queryParams(queryParams)
        .status(200)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody(expectedBody)
        .build());

    var response = dataFactoryFeignClient.performSearch(resource, queryParams, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().elements().get(0).prop("testGet").value())
        .isEqualTo("dataToSearch");
  }
}
