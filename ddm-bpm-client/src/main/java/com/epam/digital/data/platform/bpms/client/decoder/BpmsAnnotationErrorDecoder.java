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

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ErrorDecoder} error decoder that is used to
 * build error decoder chain with custom {@link BpmsResponseDecoder} decoder for mapping {@link
 * Response} response to exception.
 */
@Component
public class BpmsAnnotationErrorDecoder implements ErrorDecoder {
  
  @Setter(AccessLevel.PACKAGE)
  private ErrorDecoder errorDecoderChain;
  
  @Override
  public Exception decode(String methodKey, Response response) {
    return errorDecoderChain.decode(methodKey, response);
  }
}
