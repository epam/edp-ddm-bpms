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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to read data from
 * Data Factory
 */
@Slf4j
@RequiredArgsConstructor
@Component(DataFactoryConnectorReadDelegate.DELEGATE_NAME)
public class DataFactoryConnectorReadDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorReadDelegate";

  @SystemVariable(name = "resource")
  protected NamedVariableAccessor<String> resourceVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;
  @SystemVariable(name = "id")
  protected NamedVariableAccessor<String> resourceIdVariable;

  protected final DataFactoryFeignClient dataFactoryFeignClient;
  protected final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var id = resourceIdVariable.from(execution).get();

    log.debug("Start getting entity by id {} on resource {}", id, resource);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();

    var response = dataFactoryFeignClient.performGet(resource, id, headers);
    log.debug("Got entity by id {}", id);

    responseVariable.on(execution).set(response);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
