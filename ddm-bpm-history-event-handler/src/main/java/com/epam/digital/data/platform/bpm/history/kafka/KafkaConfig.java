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

package com.epam.digital.data.platform.bpm.history.kafka;

import com.epam.digital.data.platform.bpm.history.base.handler.ProcessPublisherHistoryEventHandler;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Class that configures an instance of {@link ProcessPublisherHistoryEventHandler} that is used
 * {@link ProcessHistoryEventKafkaPublisher Kafka implementation} of the {@link
 * ProcessHistoryEventPublisher}
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(
    prefix = "data-platform.kafka",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class KafkaConfig {

  @Bean
  @Qualifier("kafkaPublisher")
  public ProcessHistoryEventPublisher kafkaPublisher(
          KafkaTemplate<String, Object> kafkaTemplate, KafkaProperties kafkaProperties) {
    return new ProcessHistoryEventKafkaPublisher(
        kafkaTemplate, kafkaProperties);
  }

  @Bean
  @Qualifier("kafkaProcessHistoryHandler")
  public ProcessPublisherHistoryEventHandler kafkaHandler(
      @Qualifier("kafkaPublisher") ProcessHistoryEventPublisher publisher) {
    return new ProcessPublisherHistoryEventHandler(publisher);
  }
}
