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

import com.epam.digital.data.platform.bpms.extension.delegate.dto.ConnectorResponse;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component(DataFactoryConnectorBatchReadDelegate.DELEGATE_NAME)
public class DataFactoryConnectorBatchReadDelegate extends DataFactoryConnectorReadDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorBatchReadDelegate";

  @SystemVariable(name = "resourceIds")
  private NamedVariableAccessor<List<String>> resourceIdsVariable;

  @Autowired
  public DataFactoryConnectorBatchReadDelegate(RestTemplate restTemplate,
      @Value("${spring.application.name}") String springAppName,
      @Value("${camunda.system-variables.const_dataFactoryBaseUrl}") String dataFactoryBaseUrl) {
    super(restTemplate, springAppName, dataFactoryBaseUrl);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    var resource = resourceVariable.from(execution).get();
    var resourceIds = resourceIdsVariable.from(execution).getOrDefault(List.of());

    log.debug("Start executing batch read entities on resource {}", resource);
    var response = executeBatchGetOperation(execution, resource, resourceIds);
    log.debug("Finished batch read operation");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse executeBatchGetOperation(DelegateExecution execution,
      String resource, List<String> resourceIds) {
    var json = Spin.JSON("[]");

    resourceIds.stream()
        .map(id -> performGet(execution, resource, id))
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
