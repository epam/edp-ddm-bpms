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

package com.epam.digital.data.platform.bpms.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchemaKeyDeserializer extends JsonDeserializer<Schema> {

  @Override
  public Schema deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return p.readValueAs(SchemaWithMappedAdditionalProperties.class);
  }

  private static class SchemaWithMappedAdditionalProperties<T> extends Schema<T> {

    @Override
    public void setAdditionalProperties(Object additionalProperties) {
      if (Objects.isNull(additionalProperties) || additionalProperties instanceof Boolean) {
        super.setAdditionalProperties(additionalProperties);
      } else {
        var objectMapper = new ObjectMapper();
        super.setAdditionalProperties(
            objectMapper.convertValue(additionalProperties, Schema.class));
      }
    }
  }
}
