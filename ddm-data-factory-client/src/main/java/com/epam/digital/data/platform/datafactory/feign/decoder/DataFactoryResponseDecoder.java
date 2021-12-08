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

package com.epam.digital.data.platform.datafactory.feign.decoder;

import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.spin.Spin;

/**
 * The class represents an implementation of {@link Decoder} decoder that is used to decode {@link
 * Response} response to {@link ConnectorResponse} type.
 */
public class DataFactoryResponseDecoder implements Decoder {

  @Override
  public Object decode(Response response, Type type) throws IOException {
    if (response.body() == null) {
      return null;
    }
    var body = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8.name());
    var spin = StringUtils.isBlank(body) ? null : Spin.JSON(body);

    return ConnectorResponse.builder()
        .statusCode(response.status())
        .responseBody(spin)
        .headers(response.headers())
        .build();
  }
}
