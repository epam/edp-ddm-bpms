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


package com.epam.digital.data.platform.bpms.extension.delegate;


import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.bpms.extension.delegate.dto.AsyncDataLoadRequest;
import com.epam.digital.data.platform.bpms.extension.service.DigitalSystemSignatureService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import com.epam.digital.data.platform.starter.kafka.config.properties.KafkaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to asynchronous load
 * csv file data
 */
@Slf4j
@RequiredArgsConstructor
@Component(AsyncDataLoadDelegate.DELEGATE_NAME)
public class AsyncDataLoadDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "asyncDataLoadDelegate";
  public static final String ENTITY_NAME_HEADER = "EntityName";
  public static final String RESULT_VARIABLE_HEADER = "ResultVariable";

  @SystemVariable(name = "entity")
  protected NamedVariableAccessor<String> entityVariable;
  @SystemVariable(name = "file", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> fileVariable;
  @SystemVariable(name = "derivedFile", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> derivedFileVariable;
  @SystemVariable(name = "x_digital_signature_ceph_key")
  private NamedVariableAccessor<String> xDigitalSignatureCephKeyVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<String> responseVariable;

  private final HeaderBuilderFactory headerBuilderFactory;
  private final DigitalSystemSignatureService digitalSystemSignatureService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaProperties kafkaProperties;
  private final ObjectMapper mapper;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    var file = fileVariable.from(execution).get();
    var derivedFile = derivedFileVariable.from(execution).get();

    if (Objects.isNull(file)) {
      log.debug("File payload is null");
      return;
    }

    var payload = createPayloadToSign(file, derivedFile);
    var systemSignatureDto =
        DigitalSystemSignatureService.SystemSignatureDto.builder().payload(payload).build();
    var storageKey = digitalSystemSignatureService.sign(systemSignatureDto);

    var asyncDataLoadRequest = mapper.readValue(payload.toString(), AsyncDataLoadRequest.class);

    Message<AsyncDataLoadRequest> message = MessageBuilder
        .withPayload(asyncDataLoadRequest)
        .copyHeaders(createMessageHeaders(storageKey, execution))
        .build();
    kafkaTemplate.send(message);
    log.debug("Data sent to Kafka successfully");
  }

  private Map<String, String> createMessageHeaders(String signatureDerived,
      DelegateExecution execution) {
    var httpHeaders = headerBuilderFactory.builder()
        .processExecutionHttpHeaders()
        .accessTokenHeader()
        .build().toSingleValueMap();
    Map<String, String> headers = new HashMap<>(httpHeaders);
    headers.put(PlatformHttpHeader.X_DIGITAL_SIGNATURE_DERIVED.getName(), signatureDerived);
    var xDigitalSignatureCephKey = xDigitalSignatureCephKeyVariable.from(execution).get();
    headers.put(PlatformHttpHeader.X_DIGITAL_SIGNATURE.getName(), xDigitalSignatureCephKey);
    headers.put(ENTITY_NAME_HEADER, entityVariable.from(execution).get());
    headers.put(RESULT_VARIABLE_HEADER, responseVariable.from(execution).get());

    var topicName = kafkaProperties.getTopics().get("data-load-csv-topic");
    headers.put(KafkaHeaders.TOPIC, topicName);

    return headers;
  }

  private SpinJsonNode createPayloadToSign(SpinJsonNode file, SpinJsonNode derivedFile) {
    return Spin.JSON("{}")
        .prop("file", file)
        .prop("derivedFile", derivedFile);
  }
}
