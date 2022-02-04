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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
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
 * Class that configures an instance of {@link ProcessPublisherHistoryEventHandler} that is used {@link
 * ProcessHistoryEventKafkaPublisher Kafka implementation} of the {@link
 * ProcessHistoryEventPublisher}
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(prefix = "camunda.bpm.history-publisher.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

  private static final String CERTIFICATES_TYPE = "PEM";
  private static final String SECURITY_PROTOCOL = "SSL";

  private final KafkaProperties kafkaProperties;

  @Bean
  public Map<String, Object> producerConfigs() {
    var props = new HashMap<String, Object>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrap());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    if (kafkaProperties.getSsl().isEnabled()) {
      props.putAll(createSslProperties());
    }
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
  public Supplier<AdminClient> adminClientFactory() {
    return this::kafkaAdminClient;
  }

  private AdminClient kafkaAdminClient() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrap());
    if (kafkaProperties.getSsl().isEnabled()) {
      props.putAll(createSslProperties());
    }
    return AdminClient.create(props);
  }

  @Bean
  public StartupHistoryProcessKafkaTopicCreator startupHistoryProcessKafkaTopicCreator(
      Supplier<AdminClient> adminClientFactory) {
    return new StartupHistoryProcessKafkaTopicCreator(adminClientFactory, kafkaProperties);
  }

  @Bean
  @Qualifier("kafkaPublisher")
  public ProcessHistoryEventPublisher kafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    return new ProcessHistoryEventKafkaPublisher(kafkaTemplate, kafkaProperties);
  }

  @Bean
  @Qualifier("kafkaProcessHistoryHandler")
  public ProcessPublisherHistoryEventHandler kafkaHandler(
      @Qualifier("kafkaPublisher") ProcessHistoryEventPublisher publisher) {
    return new ProcessPublisherHistoryEventHandler(publisher);
  }

  private Map<String, Object> createSslProperties() {
    return Map.of(
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL,
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, CERTIFICATES_TYPE,
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, CERTIFICATES_TYPE,
            SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, kafkaProperties.getSsl().getTruststoreCertificate(),
            SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, kafkaProperties.getSsl().getKeystoreCertificate(),
            SslConfigs.SSL_KEYSTORE_KEY_CONFIG, kafkaProperties.getSsl().getKeystoreKey()
    );
  }
}
