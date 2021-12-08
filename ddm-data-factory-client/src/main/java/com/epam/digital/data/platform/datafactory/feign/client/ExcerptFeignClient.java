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

package com.epam.digital.data.platform.datafactory.feign.client;

import com.epam.digital.data.platform.datafactory.feign.config.FeignDecoderConfiguration;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The interface represents a feign client and used to perform operations in excerpt service.
 */
@FeignClient(name = "excerpt-client", url = "${excerpt-service-api.url}/excerpts", configuration = FeignDecoderConfiguration.class)
public interface ExcerptFeignClient {

  /**
   * Perform GET operation for getting excerpt by id
   *
   * @param id      excerpt identifier
   * @param headers http headers
   * @return mapped excerpt response
   * @see ConnectorResponse
   */
  @GetMapping("/{id}/status")
  ConnectorResponse performGet(@PathVariable("id") String id, @RequestHeader HttpHeaders headers);

  /**
   * Perform POST operation for excerpt creating
   *
   * @param body    request body
   * @param headers http headers
   * @return mapped excerpt response
   * @see ConnectorResponse
   */
  @PostMapping
  ConnectorResponse performPost(@RequestBody String body, @RequestHeader HttpHeaders headers);
}