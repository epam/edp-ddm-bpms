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

import com.epam.digital.data.platform.datafactory.feign.client.UserSettingsFeignClient;
import com.epam.digital.data.platform.datafactory.feign.it.builder.StubRequest;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

class UserSettingsFeignClientIT extends BaseIT {

  @Autowired
  private UserSettingsFeignClient userSettingsFeignClient;

  @Test
  void shouldPerformGet() {
    var responseBody = "{\"settings_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockUserSettingsFeignClient(StubRequest.builder()
        .path("/settings")
        .method(HttpMethod.GET)
        .requestHeaders(headers)
        .status(200)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody(responseBody)
        .build());

    var response = userSettingsFeignClient.performGet(headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().prop("settings_id").value())
        .isEqualTo("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  }

  @Test
  void shouldPerformPut() {
    var requestBody = "{\"phone\": \"string\"}";
    var responseBody = "{ \"settings_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockUserSettingsFeignClient(StubRequest.builder()
        .path("/settings")
        .method(HttpMethod.PUT)
        .requestHeaders(headers)
        .requestBody(equalTo(requestBody))
        .status(200)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody(responseBody)
        .build());

    var response = userSettingsFeignClient.performPut(requestBody, headers);

    assertThat(response).isNotNull();
    assertThat(response.getResponseBody().prop("settings_id").value())
        .isEqualTo("3fa85f64-5717-4562-b3fc-2c963f66afa6");
  }

  @Test
  void shouldThrowValidationExceptionWhenPerformPut() {
    var requestBody = "{}";
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    headers.add("X-Access-Token", "token");

    mockUserSettingsFeignClient(StubRequest.builder()
        .path("/settings")
        .method(HttpMethod.PUT)
        .requestHeaders(headers)
        .requestBody(equalTo(requestBody))
        .status(422)
        .responseHeaders(Map.of("Content-Type", List.of("application/json")))
        .responseBody("{\"traceId\":\"traceId1\",\"code\":\"Validation failed\"}")
        .build());

    var ex = assertThrows(ValidationException.class,
        () -> userSettingsFeignClient.performPut(requestBody, headers));

    assertThat(ex).isNotNull();
    assertThat(ex.getCode()).isEqualTo("Validation failed");
  }
}
