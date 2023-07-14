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

package com.epam.digital.data.platform.bpms.extension.it.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.AsyncDataLoadResponse;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.AsyncDataLoadResult;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.RequestContext;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.Result;
import com.epam.digital.data.platform.bpms.extension.it.BaseIT;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class AsyncDataLoadResponseKafkaListenerIT extends BaseIT {

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Autowired
  private KafkaProperties kafkaProperties;

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  @Deployment(resources = {"bpmn/listener/testAsyncDataLoadListener.bpmn"})
  void shouldReceiveKafkaMessage(String status, String details) {
    var resultVariableName = "response";
    var entityName = "item";
    var processInstance = runtimeService
        .startProcessInstanceByKey("testAsyncDataLoadListener_key");
    var payload = AsyncDataLoadResponse.builder()
        .payload(AsyncDataLoadResult.builder()
            .resultVariable(resultVariableName)
            .entityName(entityName)
            .build())
        .status(status)
        .details(details)
        .requestContext(RequestContext.builder()
            .businessProcessInstanceId(processInstance.getProcessInstanceId())
            .build())
        .build();
    var expectedResult = new Result(status, details);
    Message<AsyncDataLoadResponse> message = MessageBuilder
        .withPayload(payload)
        .copyHeaders(Map.of(TOPIC, kafkaProperties.getAdditionalTopics().get("data-load-csv-topic-outbound")))
        .build();

    kafkaTemplate.send(message);

    await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      BpmnAwareTests.assertThat(processInstance).variables().containsKey(resultVariableName);
      BpmnAwareTests.assertThat(processInstance).variables().containsValue(expectedResult);
      BpmnAwareTests.assertThat(processInstance).isEnded();
    });
  }

  static Stream<Arguments> testArgumentProvider() {
    return Stream.of(
        arguments("SUCCESS", "OK"),
        arguments("CONSTRAINT_VIOLATION", "error: constrain violation"),
        arguments("OPERATION_FAILED", "operation failed message")
    );
  }
}
