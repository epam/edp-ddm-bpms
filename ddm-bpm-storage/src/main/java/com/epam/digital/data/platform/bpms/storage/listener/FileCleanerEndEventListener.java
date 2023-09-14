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

package com.epam.digital.data.platform.bpms.storage.listener;

import com.epam.digital.data.platform.bpms.api.dto.FileStorageCleanupDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.dgtldcmnt.client.DigitalDocumentServiceRestClient;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove files from storage before the completion of the business process instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanerEndEventListener implements ExecutionListener {

  @VisibleForTesting
  static final String TOPIC_KEY = "lowcode-file-storage-cleanup-topic";

  @Qualifier("system-user-keycloak-client-service")
  private final IdmService idmService;
  private final DigitalDocumentServiceRestClient digitalDocumentServiceRestClient;

  @Value("${data-platform.kafka.enabled:true}")
  private final boolean kafkaEnabled;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  @Lazy
  private final KafkaProperties kafkaProperties;

  @Override
  public void notify(DelegateExecution execution) {
    if (kafkaEnabled) {
      sendToKafka(execution);
    } else {
      sendThroughHttp(execution);
    }
  }

  private void sendThroughHttp(DelegateExecution execution) {
    var processInstanceId = execution.getProcessInstanceId();
    try {
      digitalDocumentServiceRestClient.delete(processInstanceId, createHeaders());
    } catch (RuntimeException ex) {
      log.warn("Error while deleting documents, processDefinitionId={}, processInstanceId={}",
          execution.getProcessDefinitionId(), processInstanceId, ex);
    }
  }

  private HttpHeaders createHeaders() {
    var headers = new HttpHeaders();
    headers.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), idmService.getClientAccessToken());
    var requestXsrfToken = UUID.randomUUID().toString();
    headers.add(PlatformHttpHeader.X_XSRF_TOKEN.getName(), requestXsrfToken);
    headers.add("Cookie", String.format("%s=%s", "XSRF-TOKEN", requestXsrfToken));
    return headers;
  }

  private void sendToKafka(DelegateExecution execution) {
    var topic = kafkaProperties.getTopics().get(TOPIC_KEY);
    var processInstanceId = execution.getProcessInstanceId();
    var data = new FileStorageCleanupDto(processInstanceId);

    var future = kafkaTemplate.send(topic, data);

    future.addCallback(
        result -> log.info("Successful sent message about file cleanup in process {}",
            processInstanceId),
        ex -> log.error("Failed sending message about file cleanup in process {}. Cause : {}",
            processInstanceId, ex.getMessage(), ex)
    );
  }
}
