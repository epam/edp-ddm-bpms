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

package com.epam.digital.data.platform.bpms.client.config;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.client.BaseFeignClient;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * The class represents a configuration for all feign clients.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = BaseFeignClient.class)
@EnableFeignClients(basePackageClasses = BaseFeignClient.class)
public class FeignConfig {

  private static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";

  @Bean
  public RequestInterceptor csrfRequestInterceptor() {
    return requestTemplate -> {
      var requestXsrfToken = UUID.randomUUID().toString();
      requestTemplate.header(PlatformHttpHeader.X_XSRF_TOKEN.getName(), requestXsrfToken);
      requestTemplate.header("Cookie", String.format("%s=%s", XSRF_COOKIE_NAME, requestXsrfToken));
    };
  }
}