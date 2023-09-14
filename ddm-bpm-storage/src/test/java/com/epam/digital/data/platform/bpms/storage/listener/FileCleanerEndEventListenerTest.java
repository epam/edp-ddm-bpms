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

package com.epam.digital.data.platform.bpms.storage.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.digital.data.platform.bpms.api.dto.FileStorageCleanupDto;
import com.epam.digital.data.platform.dgtldcmnt.client.DigitalDocumentServiceRestClient;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import java.util.Map;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

@ExtendWith(MockitoExtension.class)
class FileCleanerEndEventListenerTest {

  private static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private static final String TOPIC_NAME = "bpm-lowcode-file-storage-cleanup";

  @Mock
  private ExecutionEntity executionEntity;
  @Mock
  private DigitalDocumentServiceRestClient client;
  @Mock
  private IdmService idmService;
  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Mock
  private KafkaProperties kafkaProperties;
  private FileCleanerEndEventListener fileCleanerEndEventListener;

  @BeforeEach
  void setUp() {
    Mockito.doReturn(PROCESS_INSTANCE_ID).when(executionEntity).getProcessInstanceId();
  }

  @Test
  void shouldDeleteFilesByListOfKeys_http() {
    fileCleanerEndEventListener = new FileCleanerEndEventListener(idmService, client, false,
        kafkaTemplate, kafkaProperties);

    Mockito.doReturn("token").when(idmService).getClientAccessToken();

    fileCleanerEndEventListener.notify(executionEntity);

    verify(executionEntity).getProcessInstanceId();
    verify(client).delete(eq(PROCESS_INSTANCE_ID), any());
    verifyNoInteractions(kafkaProperties, kafkaTemplate);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDeleteFilesByListOfKeys_kafka() {
    fileCleanerEndEventListener = new FileCleanerEndEventListener(idmService, client, true,
        kafkaTemplate, kafkaProperties);

    final var topics = Map.of(FileCleanerEndEventListener.TOPIC_KEY, TOPIC_NAME);
    Mockito.doReturn(topics).when(kafkaProperties).getTopics();
    final var expectedData = new FileStorageCleanupDto(PROCESS_INSTANCE_ID);
    final var resultFuture = Mockito.mock(ListenableFuture.class);
    Mockito.doReturn(resultFuture).when(kafkaTemplate).send(eq(TOPIC_NAME), refEq(expectedData));

    fileCleanerEndEventListener.notify(executionEntity);

    verify(kafkaTemplate).send(eq(TOPIC_NAME), refEq(expectedData));
    verify(resultFuture).addCallback(any(SuccessCallback.class), any(FailureCallback.class));
    verifyNoInteractions(client, idmService);
  }
}
