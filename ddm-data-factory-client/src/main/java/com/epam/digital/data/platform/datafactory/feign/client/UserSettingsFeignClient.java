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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The interface represents a feign client and used to perform operations in user settings service.
 */
@FeignClient(name = "user-settings-client", url = "${user-settings-service-api.url}/settings", configuration = FeignDecoderConfiguration.class)
public interface UserSettingsFeignClient {

  /**
   * Perform GET operation for getting user settings
   *
   * @param headers http headers
   * @return mapped user-settings response
   * @see ConnectorResponse
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  ConnectorResponse performGet(@RequestHeader HttpHeaders headers);

  /**
   * Perform PUT operation for updating user settings
   *
   * @param body    request body
   * @param headers http headers
   * @return mapped user-settings response
   * @see ConnectorResponse
   */
  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  ConnectorResponse performPut(@RequestBody String body, @RequestHeader HttpHeaders headers);
}
