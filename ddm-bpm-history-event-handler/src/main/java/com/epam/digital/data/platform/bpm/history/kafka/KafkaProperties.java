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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class that is used for configuration kafka history publisher and its
 * topics
 * <p>
 * Represents next structure under the prefix {@code camunda.bpm.history-publisher}:
 * <pre>
 * kafka:
 *   enabled: true # enable kafka history publisher or not
 *   bootstrap: localhost:9092 # kafka bootstrap url
 *   topics:
 *     history-process-instance-topic:
 *       name: history-process-instance-topic-name # name of the topic
 *     history-task-topic:
 *       name: history-task-topic-name
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "camunda.bpm.history-publisher.kafka")
public class KafkaProperties {

  private boolean enabled;
  private String bootstrap;

  private TopicsProperty topics;

  @Data
  public static class TopicsProperty {

    private TopicProperties historyProcessInstanceTopic;
    private TopicProperties historyTaskTopic;
  }

  @Data
  public static class TopicProperties {

    private String name;
  }
}
