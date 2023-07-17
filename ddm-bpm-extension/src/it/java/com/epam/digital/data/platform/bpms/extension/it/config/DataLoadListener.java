/*
 * Copyright 2023 EPAM Systems.
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

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.AsyncDataLoadRequest;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DataLoadListener {

  private Map<String, Message<AsyncDataLoadRequest>> storage;

  @KafkaListener(
      topics = "#{kafkaProperties.topics.get('data-load-csv-topic-inbound')}",
      groupId = "#{kafkaProperties.consumer.groupId}",
      containerFactory = "concurrentKafkaListenerContainerFactory", autoStartup = "true")
  public void receive(Message<AsyncDataLoadRequest> input) {
    storage.put((String) input.getHeaders()
        .get(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID.getName()), input);
  }

  public Map<String, Message<AsyncDataLoadRequest>> getStorage() {
    return storage;
  }
}
