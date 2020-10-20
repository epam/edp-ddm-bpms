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

import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.factory.client.DataFactoryFeignClient;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component(DataFactoryConnectorBatchReadDelegate.DELEGATE_NAME)
public class DataFactoryConnectorBatchReadDelegate extends DataFactoryConnectorReadDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorBatchReadDelegate";

  @SystemVariable(name = "resourceIds")
  private NamedVariableAccessor<List<String>> resourceIdsVariable;

  public DataFactoryConnectorBatchReadDelegate(DataFactoryFeignClient dataFactoryFeignClient,
      HeaderBuilderFactory headerBuilderFactory) {
    super(dataFactoryFeignClient, headerBuilderFactory);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var resourceIds = resourceIdsVariable.from(execution).getOrDefault(List.of());

    log.debug("Start executing batch read entities on resource {}", resource);
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();

    var response = executeBatchGetOperation(resource, resourceIds, headers);
    log.debug("Finished batch read operation");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse executeBatchGetOperation(String resource, List<String> resourceIds,
      HttpHeaders headers) {
    var json = Spin.JSON("[]");

    resourceIds.stream()
        .map(id -> dataFactoryFeignClient.performGet(resource, id, headers))
        .map(ConnectorResponse::getResponseBody).forEach(json::append);

    return ConnectorResponse.builder()
        .statusCode(HttpStatus.OK.value())
        .responseBody(json)
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
