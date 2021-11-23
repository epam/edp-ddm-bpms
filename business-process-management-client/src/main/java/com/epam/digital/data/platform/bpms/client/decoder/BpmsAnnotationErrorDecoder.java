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

import com.epam.digital.data.platform.bpms.client.BaseFeignClient;
import feign.Response;
import feign.codec.ErrorDecoder;
import feign.error.AnnotationErrorDecoder;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ErrorDecoder} error decoder that is used to
 * build error decoder chain with custom {@link BpmsResponseDecoder} decoder for mapping {@link
 * Response} response to exception.
 */
@Component
public class BpmsAnnotationErrorDecoder implements ErrorDecoder {

  @Autowired
  private List<BaseFeignClient> baseFeignClients;
  @Autowired
  private BpmsResponseDecoder bpmsResponseDecoder;

  private ErrorDecoder errorDecoderChain;

  @PostConstruct
  public void init() {
    errorDecoderChain = new Default();

    for (BaseFeignClient baseFeignClient : baseFeignClients) {
      Class<?> feignClientType = baseFeignClient.getClass().getInterfaces()[0];

      errorDecoderChain = AnnotationErrorDecoder.builderFor(feignClientType)
          .withDefaultDecoder(errorDecoderChain)
          .withResponseBodyDecoder(bpmsResponseDecoder).build();
    }
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    return errorDecoderChain.decode(methodKey, response);
  }
}
