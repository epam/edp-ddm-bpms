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

package com.epam.digital.data.platform.bpms.extension.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.epam.digital.data.platform.bpms.extension.delegate.AsyncDataLoadDelegate;
import com.epam.digital.data.platform.bpms.extension.it.config.DataLoadListener;
import com.epam.digital.data.platform.dso.api.dto.SignResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;


public class AsyncDataLoadDelegateIT extends BaseIT {

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;
  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  @Autowired
  private DataLoadListener listener;

  @Before
  public void setUp() {
    kafkaListenerEndpointRegistry.getListenerContainers().forEach(container ->
        ContainerTestUtils.waitForAssignment(container,
            embeddedKafkaBroker.getPartitionsPerTopic()));
  }

  @Test
  @Deployment(resources = {"bpmn/delegate/testAsyncDataLoadDelegate.bpmn"})
  public void shouldSendMessageToKafka() throws JsonProcessingException {
    var response = SignResponseDto.builder().signature("test").build();
    var expectedPayload = "{\"file\":{\"checksum\":\"test-checksum\",\"id\":\"file-id\"},\"derivedFile\":{\"checksum\":\"derived-checksum\",\"id\":\"derived-file-id\"}}";

    digitalSignatureMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo(
                "{\"data\":\"{\\\"file\\\":{\\\"checksum\\\":\\\"test-checksum\\\",\\\"id\\\":\\\"file-id\\\"},\\\"derivedFile\\\":{\\\"checksum\\\":\\\"derived-checksum\\\",\\\"id\\\":\\\"derived-file-id\\\"}}\"}"))
            .willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(200)
                    .withBody(objectMapper.writeValueAsString(response)))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testAsyncDataLoadDelegate_key");

    BpmnAwareTests.assertThat(processInstance).isEnded();

    await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      var storage = listener.getStorage();
      assertThat(storage).hasSize(1);
      var message = storage.get(processInstance.getProcessInstanceId());
      assertThat(message.getPayload()).isEqualTo(expectedPayload);

      var headers = message.getHeaders();
      assertThat(headers).containsEntry(AsyncDataLoadDelegate.ENTITY_NAME_HEADER, "test");
      assertThat(headers).containsEntry(AsyncDataLoadDelegate.RESULT_VARIABLE_HEADER, "resultVariable");
    });
  }
}
