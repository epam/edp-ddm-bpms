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

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * {@link ProcessHistoryEventPublisher} implementation which sends history dtos to Kafka
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessHistoryEventKafkaPublisher implements ProcessHistoryEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaProperties kafkaProperties;

  @Override
  public void put(HistoryProcess dto) {
    sendHistoryProcessInstanceDto(dto);
  }

  @Override
  public void patch(HistoryProcess dto) {
    sendHistoryProcessInstanceDto(dto);
  }

  @Override
  public void put(HistoryTask dto) {
    sendHistoryTaskDto(dto);
  }

  @Override
  public void patch(HistoryTask dto) {
    sendHistoryTaskDto(dto);
  }

  private void sendHistoryProcessInstanceDto(HistoryProcess dto) {
    var topicName = kafkaProperties.getTopics().get("history-process-instance-topic");
    send(topicName, dto);
  }

  private void sendHistoryTaskDto(HistoryTask dto) {
    var topicName = kafkaProperties.getTopics().get("history-task-topic");
    send(topicName, dto);
  }

  private <T> void send(String topic, T dto) {
    var future = kafkaTemplate.send(topic, dto);

    future.addCallback(result -> log.debug("Successful sending message {} to topic {}", dto, topic),
        ex -> log.warn("Sending message {} to topic {} failed: Cause {}", dto, topic,
            ex.getMessage(), ex));
  }
}
