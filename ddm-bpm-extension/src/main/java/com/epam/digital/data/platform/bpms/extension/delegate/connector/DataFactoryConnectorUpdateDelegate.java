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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.factory.client.DataFactoryFeignClient;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to update
 * data in Data Factory
 */
@Slf4j
@RequiredArgsConstructor
@Component("dataFactoryConnectorUpdateDelegate")
public class DataFactoryConnectorUpdateDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorUpdateDelegate";

  @SystemVariable(name = "resource")
  protected NamedVariableAccessor<String> resourceVariable;
  @SystemVariable(name = "id")
  protected NamedVariableAccessor<String> resourceIdVariable;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  private final DataFactoryFeignClient dataFactoryFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws JsonProcessingException {
    var resource = resourceVariable.from(execution).get();
    var id = resourceIdVariable.from(execution).get();
    var payload = payloadVariable.from(execution).getOptional();

    log.debug("Start updating entity with id {} on resource {}", id, resource);
    var requestBody = payload.map(Object::toString).orElse(null);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();
    var response = dataFactoryFeignClient.performPut(resource, id, requestBody, headers);
    log.debug("Entity with id {} successfully updated", id);

    responseVariable.on(execution).set(response);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
