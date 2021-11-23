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

package com.epam.digital.data.platform.bpms.client.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link Decoder} decoder that is used to decode {@link
 * Response} response by {@link Type} type.
 */
@Component
public class BpmsResponseDecoder implements Decoder {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Object decode(Response response, Type type) throws IOException {
    if (response.body() == null || HttpStatus.NO_CONTENT.value() == response.status()) {
      return null;
    }
    return objectMapper.readValue(
        Util.toByteArray(response.body().asInputStream()), objectMapper.constructType(type));
  }
}
