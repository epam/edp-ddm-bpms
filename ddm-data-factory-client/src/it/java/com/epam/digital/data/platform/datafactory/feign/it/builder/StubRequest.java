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

package com.epam.digital.data.platform.datafactory.feign.it.builder;

import com.github.tomakehurst.wiremock.matching.ContentPattern;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Builder
@Getter
public class StubRequest {

  private final String path;
  private final HttpMethod method;
  @Default
  private final Map<String, String> queryParams = Map.of();
  @Default
  private final HttpHeaders requestHeaders = HttpHeaders.EMPTY;
  private final ContentPattern<String> requestBody;
  private final int status;
  private final String responseBody;
  @Default
  private final Map<String, List<String>> responseHeaders = Map.of();
}
