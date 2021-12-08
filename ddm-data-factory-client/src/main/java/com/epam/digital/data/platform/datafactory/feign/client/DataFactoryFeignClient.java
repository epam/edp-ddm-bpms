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
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The interface represents a feign client and used to perform operations in data factory service.
 */
@FeignClient(name = "data-factory-client", url = "${camunda.system-variables.const_dataFactoryBaseUrl}", configuration = FeignDecoderConfiguration.class)
public interface DataFactoryFeignClient {

  /**
   * Perform GET operation for getting data factory entity by id
   *
   * @param resource url resource
   * @param id       identifier for resource entity
   * @param headers  http headers
   * @return mapped response
   * @see ConnectorResponse
   */
  @GetMapping("/{resource}/{id}")
  ConnectorResponse performGet(@PathVariable("resource") String resource,
      @PathVariable("id") String id, @RequestHeader HttpHeaders headers);

  /**
   * Perform POST operation for creating data factory entity
   *
   * @param resource url resource
   * @param headers  http headers
   * @return mapped response
   * @see ConnectorResponse
   */
  @PostMapping("/{resource}")
  ConnectorResponse performPost(@PathVariable("resource") String resource,
      @RequestBody String body, @RequestHeader HttpHeaders headers);

  /**
   * Perform PUT operation for updating data factory entity by id
   *
   * @param resource url resource
   * @param id       identifier for resource entity
   * @param body     request body
   * @param headers  http headers
   * @return mapped response
   * @see ConnectorResponse
   */
  @PutMapping("/{resource}/{id}")
  ConnectorResponse performPut(@PathVariable("resource") String resource,
      @PathVariable("id") String id, @RequestBody String body, @RequestHeader HttpHeaders headers);

  /**
   * Perform DELETE operation for deleting data factory entity by id
   *
   * @param resource url resource
   * @param id       identifier for resource entity
   * @param headers  http headers
   * @return mapped response
   * @see ConnectorResponse
   */
  @DeleteMapping("/{resource}/{id}")
  ConnectorResponse performDelete(@PathVariable("resource") String resource,
      @PathVariable("id") String id, @RequestHeader HttpHeaders headers);

  /**
   * Perform GET operation for searching data factory entities by query params
   *
   * @param resource url resource
   * @param params   query search params
   * @param headers  http headers
   * @return mapped response
   * @see ConnectorResponse
   */
  @GetMapping("/{resource}")
  ConnectorResponse performSearch(@PathVariable("resource") String resource,
      @SpringQueryMap Map<String, String> params, @RequestHeader HttpHeaders headers);
}
