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

package com.epam.digital.data.platform.bpms.extension.delegate.connector;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.factory.client.DataFactoryFeignClient;
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
@Component(DataFactoryConnectorSearchDelegate.DELEGATE_NAME)
public class DataFactoryConnectorSearchDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorSearchDelegate";

  @SystemVariable(name = "resource")
  protected NamedVariableAccessor<String> resourceVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;
  @SystemVariable(name = "searchConditions")
  private NamedVariableAccessor<Map<String, Object>> searchConditionsVariable;

  private final DataFactoryFeignClient dataFactoryFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) {
    responseVariable.on(execution).set(ConnectorResponse.builder().build());

    var resource = resourceVariable.from(execution).get();
    var searchConditions = searchConditionsVariable.from(execution).getOrDefault(Map.of());

    log.debug("Start searching entities on resource {}", resource);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();

    var response = dataFactoryFeignClient.performSearch(resource, searchConditions, headers);
    log.debug("Found entities on resource {}", resource);

    responseVariable.on(execution).set(response);
  }

  @Override
  public String getDelegateName() {
    return null;
  }
}
