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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.registry;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.factory.client.PlatformGatewayFeignClient;
import com.epam.digital.data.platform.datafactory.feign.model.request.StartBpRequest;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to search data in
 * Data Factory
 */
@Slf4j
@RequiredArgsConstructor
@Component(RegistryConnectorStartBpDelegate.DELEGATE_NAME)
public class RegistryConnectorStartBpDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "registryConnectorStartBpDelegate";

  @SystemVariable(name = "registry")
  protected NamedVariableAccessor<String> registryVariable;
  @SystemVariable(name = "businessProcessKey")
  protected NamedVariableAccessor<String> businessProcessKeyVariable;
  @SystemVariable(name = "startVariables")
  private NamedVariableAccessor<Map<String, String>> startVariablesVariable;

  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;

  private final PlatformGatewayFeignClient platformGatewayFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) {
    responseVariable.on(execution).set(ConnectorResponse.builder().build());

    var registry = registryVariable.from(execution).get();
    var businessProcessKey = businessProcessKeyVariable.from(execution).get();
    var startVariables = startVariablesVariable.from(execution).getOrDefault(Map.of());

    log.debug("Start {} business process in registry {}", businessProcessKey, registry);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .accessTokenHeader()
        .build();
    var startBpRequest = StartBpRequest.builder()
        .businessProcessDefinitionKey(businessProcessKey)
        .startVariables(startVariables)
        .build();

    var response = platformGatewayFeignClient.startBp(registry, startBpRequest, headers);
    log.debug("Business process {} in registry {} has finished", businessProcessKey, registry);

    responseVariable.on(execution).set(response);
  }

  @Override
  public String getDelegateName() {
    return null;
  }
}
