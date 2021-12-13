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

import com.epam.digital.data.platform.bpm.history.base.handler.ProcessHistoryEventHandler;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Class that configures an instance of {@link ProcessHistoryEventHandler} that is used {@link
 * ProcessHistoryEventKafkaPublisher Kafka implementation} of the {@link
 * ProcessHistoryEventPublisher}
 */
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(prefix = "camunda.bpm.history-publisher.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

  @Autowired
  private KafkaProperties kafkaProperties;

  @Bean
  public Map<String, Object> producerConfigs() {
    var props = new HashMap<String, Object>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrap());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  @Primary
  public ProducerFactory<String, Object> requestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, Object> replyingKafkaTemplate(ProducerFactory<String, Object> pf) {
    return new KafkaTemplate<>(pf);
  }

  @Bean
  @Qualifier("kafkaPublisher")
  public ProcessHistoryEventPublisher kafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    return new ProcessHistoryEventKafkaPublisher(kafkaTemplate, kafkaProperties);
  }

  @Bean
  @Qualifier("kafkaProcessHistoryHandler")
  public ProcessHistoryEventHandler kafkaHandler(
      @Qualifier("kafkaPublisher") ProcessHistoryEventPublisher publisher) {
    return new ProcessHistoryEventHandler(publisher);
  }
}
