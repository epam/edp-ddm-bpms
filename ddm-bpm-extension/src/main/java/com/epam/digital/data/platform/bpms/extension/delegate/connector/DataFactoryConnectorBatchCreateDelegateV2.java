/*
 * Copyright 2022 EPAM Systems.
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

import static com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchCreateDelegateV2.DataUploadType.CSV;
import static com.epam.digital.data.platform.bpms.extension.delegate.connector.DataFactoryConnectorBatchCreateDelegateV2.DataUploadType.LIST;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder.HeaderBuilderFactory;
import com.epam.digital.data.platform.bpms.extension.service.DigitalSystemSignatureService;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.datafactory.factory.client.DataFactoryFeignClient;
import com.epam.digital.data.platform.datafactory.feign.model.response.ConnectorResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used to execute data
 * batch creation in Data Factory
 */
@Slf4j
@RequiredArgsConstructor
@Component(DataFactoryConnectorBatchCreateDelegateV2.DELEGATE_NAME)
public class DataFactoryConnectorBatchCreateDelegateV2 extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "dataFactoryConnectorBatchCreateDelegateV2";

  @SystemVariable(name = "resource")
  protected NamedVariableAccessor<String> resourceVariable;
  @SystemVariable(name = "payload", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> payloadVariable;
  @SystemVariable(name = "response", isTransient = true)
  protected NamedVariableAccessor<ConnectorResponse> responseVariable;
  @SystemVariable(name = "x_digital_signature_derived_ceph_key")
  private NamedVariableAccessor<String> xDigitalSignatureDerivedStorageKeyVariable;
  @SystemVariable(name = "uploadType")
  private NamedVariableAccessor<String> uploadType;

  private final DigitalSystemSignatureService digitalSystemSignatureService;
  private final DataFactoryFeignClient dataFactoryFeignClient;
  private final HeaderBuilderFactory headerBuilderFactory;

  @Override
  public void executeInternal(DelegateExecution execution) throws Exception {
    var resource = resourceVariable.from(execution).get();
    var payload = payloadVariable.from(execution).getOrDefault(Spin.JSON(Map.of()));
    var uploadTypeString = uploadType.from(execution).getOrThrow();

    log.debug("Start executing batch create entities on resource {}, using {} upload type", resource, uploadTypeString);
    ConnectorResponse response = null;
    if (LIST.toString().equals(uploadTypeString)) {
      var dataFactoryPayload = Spin.JSON(Map.of("entities", payload.unwrap()));
      response = executeBatchCreateOperation(execution, dataFactoryPayload, resource, uploadTypeString);
    } else if (CSV.toString().equals(uploadTypeString)) {
      response = executeBatchCreateOperation(execution, payload, resource, uploadTypeString);
    }
    log.debug("Finished batch create operation");

    responseVariable.on(execution).set(response);
  }

  private ConnectorResponse executeBatchCreateOperation(DelegateExecution execution,
      SpinJsonNode payload, String resource, String creationType) throws Exception {
    signNode(execution, payload);

    log.debug("Start creating entities");
    var headers = headerBuilderFactory.builder()
        .contentTypeJson()
        .processExecutionHttpHeaders()
        .digitalSignatureHttpHeaders()
        .accessTokenHeader()
        .build();
    dataFactoryFeignClient.performPostBatch(resource, creationType.toLowerCase(), payload.toString(), headers);
    log.debug("Entities was created successfully");
    return ConnectorResponse.builder()
        .statusCode(HttpStatus.CREATED.value())
        .build();
  }

  private void signNode(DelegateExecution execution, SpinJsonNode stringJsonNode) throws Exception {
    var systemSignatureDto =
        DigitalSystemSignatureService.SystemSignatureDto.builder()
            .payload(stringJsonNode)
            .build();
    var systemSignatureStorageKey = digitalSystemSignatureService.sign(systemSignatureDto);
    xDigitalSignatureDerivedStorageKeyVariable.on(execution).set(systemSignatureStorageKey);
  }

  enum DataUploadType {
    LIST,
    CSV
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
