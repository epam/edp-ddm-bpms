/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.deserializer.SchemaKeyDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

  private final ObjectMapper objectMapper;

  @Bean
  public OpenAPI openAPI() {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Schema.class, new SchemaKeyDeserializer());
    objectMapper.registerModule(module);

    try {
      var extendedOpenapiStream = this.getClass().getClassLoader()
          .getResourceAsStream("extended-openapi.json");
      var openapiStream = this.getClass().getClassLoader().getResourceAsStream("openapi.json");

      var openapiJsonNode = objectMapper.readValue(openapiStream, JsonNode.class);
      openapiJsonNode = objectMapper.readerForUpdating(openapiJsonNode)
          .readValue(extendedOpenapiStream);

      var openApi = objectMapper.convertValue(openapiJsonNode, OpenAPI.class);
      openApi.servers(openApi.getServers()
          .subList(openApi.getServers().size() - 2, openApi.getServers().size()));
      return openApi;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
