/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.it.config;

import com.epam.digital.data.platform.integration.ceph.config.S3ConfigProperties;
import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.storage.message.config.MessagePayloadRedisStorageConfiguration;
import com.epam.digital.data.platform.storage.message.factory.MessagePayloadStorageServiceFactory;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "storage.message-payload", name = "type", havingValue = "redis")
public class TestRedisMessagePayloadStorageConfig {

  @Bean
  public MessagePayloadStorageServiceFactory messagePayloadStorageServiceFactory(ObjectMapper objectMapper,
      CephS3Factory cephS3Factory) {
    return new MessagePayloadStorageServiceFactory(objectMapper, cephS3Factory);
  }

  @Bean
  @ConfigurationProperties(prefix = "storage.backend.redis")
  public MessagePayloadRedisStorageConfiguration messagePayloadRedisStorageConfiguration() {
    return new MessagePayloadRedisStorageConfiguration();
  }

  @Bean
  public MessagePayloadStorageService messagePayloadStorageService(
      MessagePayloadStorageServiceFactory factory,
      MessagePayloadRedisStorageConfiguration config) {
    return factory.redisMessagePayloadStorageService(config);
  }

  @Bean
  public CephS3Factory cephS3Factory() {
    return new CephS3Factory(new S3ConfigProperties());
  }
}
