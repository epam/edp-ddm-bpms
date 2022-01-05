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
import feign.codec.ErrorDecoder;
import feign.codec.ErrorDecoder.Default;
import feign.error.AnnotationErrorDecoder;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeignAnnotationErrorDecoderInitializer {
  
  @Autowired
  private List<BaseFeignClient> baseFeignClients;
  
  @Autowired
  BpmsAnnotationErrorDecoder bpmsAnnotationErrorDecoder;
  @Autowired
  BpmsResponseDecoder bpmsResponseDecoder;

  @PostConstruct
  public void init() {
    ErrorDecoder errorDecoderChain = new Default();

    for (BaseFeignClient baseFeignClient : baseFeignClients) {
      Class<?> feignClientType = baseFeignClient.getClass().getInterfaces()[0];

      errorDecoderChain = AnnotationErrorDecoder.builderFor(feignClientType)
          .withDefaultDecoder(errorDecoderChain)
          .withResponseBodyDecoder(bpmsResponseDecoder).build();
    }

    bpmsAnnotationErrorDecoder.setErrorDecoderChain(errorDecoderChain);
  }
  
}
