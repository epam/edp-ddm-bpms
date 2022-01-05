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

package com.epam.digital.data.platform.bpm.history.it.kafka;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bpm.history.it.storage.TestHistoryEventStorage;
import com.epam.digital.data.platform.bpm.history.kafka.StartupHistoryProcessKafkaTopicCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
@Getter
@Component
public class KafkaConsumer {

  private final TestHistoryEventStorage storage = new TestHistoryEventStorage();

  @Inject
  private StartupHistoryProcessKafkaTopicCreator startupHistoryProcessKafkaTopicCreator;
  @Inject
  private EmbeddedKafkaBroker embeddedKafkaBroker;
  @Inject
  private ObjectMapper objectMapper;

  private Consumer<String, String> consumer;

  @PostConstruct
  public void setUp() {
    ReflectionTestUtils.setField(embeddedKafkaBroker, "topics",
        new HashSet<>(Set.of("bpm-history-process", "bpm-history-task")));
    var props = KafkaTestUtils.consumerProps("bpm", "false", embeddedKafkaBroker);
    var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(props);
    consumer = consumerFactory.createConsumer();
  }

  public void consumeAll() {
    embeddedKafkaBroker.consumeFromEmbeddedTopics(consumer,
        "bpm-history-process", "bpm-history-task");
    KafkaTestUtils.getRecords(consumer).forEach(record -> {
      if (record.topic().equals("bpm-history-process")) {
        consumeHistoryProcessInstanceDto(record);
      } else if (record.topic().equals("bpm-history-task")) {
        consumeHistoryTaskDto(record);
      }
    });
    consumer.close();
  }

  @SneakyThrows
  public void consumeHistoryProcessInstanceDto(ConsumerRecord<String, String> consumerRecord) {
    var value = objectMapper.readValue(consumerRecord.value(), HistoryProcess.class);
    log.info("bpm-history-process event - {}", value);

    if (Objects.isNull(storage.getHistoryProcessInstanceDto(value.getProcessInstanceId()))) {
      storage.put(value);
    } else {
      storage.patch(value);
    }
  }

  @SneakyThrows
  public void consumeHistoryTaskDto(ConsumerRecord<String, String> consumerRecord) {
    var value = objectMapper.readValue(consumerRecord.value(), HistoryTask.class);

    log.info("bpm-history-task event - {}", value);

    if (Objects.isNull(storage.getHistoryTaskDto(value.getActivityInstanceId()))) {
      storage.put(value);
    } else {
      storage.patch(value);
    }
  }
}
