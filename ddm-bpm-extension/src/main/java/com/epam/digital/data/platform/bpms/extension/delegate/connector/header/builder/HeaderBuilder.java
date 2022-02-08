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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder;

import com.epam.digital.data.platform.bpms.api.dto.enums.PlatformHttpHeader;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.context.DelegateExecutionContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

/**
 * The class represents a builder that is used for collecting headers from the delegate execution
 * context.
 */
@RequiredArgsConstructor
public class HeaderBuilder {

  private static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";

  private final NamedVariableAccessor<String> xAccessTokenVariable;
  private final NamedVariableAccessor<String> xDigitalSignatureCephKeyVariable;
  private final NamedVariableAccessor<String> xDigitalSignatureDerivedCephKeyVariable;
  private final String springAppName;

  private final HttpHeaders httpHeadersList = new HttpHeaders();

  public HeaderBuilder processExecutionHttpHeaders() {
    var execution = DelegateExecutionContext.getCurrentDelegationExecution();
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_SYSTEM.getName(), "Low-code Platform");
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_APPLICATION.getName(), springAppName);
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS.getName(),
        ((ExecutionEntity) execution).getProcessDefinition().getKey());
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_BUSINESS_ACTIVITY.getName(),
        execution.getCurrentActivityId());
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_BUSINESS_ACTIVITY_INSTANCE_ID.getName(),
        execution.getActivityInstanceId());
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS_INSTANCE_ID.getName(),
        execution.getProcessInstanceId());
    httpHeadersList.add(PlatformHttpHeader.X_SOURCE_BUSINESS_PROCESS_DEFINITION_ID.getName(),
        execution.getProcessDefinitionId());
    return this;
  }

  public HeaderBuilder digitalSignatureHttpHeaders() {
    var execution = DelegateExecutionContext.getCurrentDelegationExecution();
    var xDigitalSignatureCephKey = xDigitalSignatureCephKeyVariable.from(execution).get();
    httpHeadersList.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE.getName(), xDigitalSignatureCephKey);
    var xDigitalSignatureDerivedCephKey = xDigitalSignatureDerivedCephKeyVariable
        .from(execution).get();
    if (!StringUtils.isBlank(xDigitalSignatureDerivedCephKey)) {
      httpHeadersList.add(PlatformHttpHeader.X_DIGITAL_SIGNATURE_DERIVED.getName(),
          xDigitalSignatureDerivedCephKey);
    }
    return this;
  }

  public HeaderBuilder accessTokenHeader() {
    var execution = DelegateExecutionContext.getCurrentDelegationExecution();
    xAccessTokenVariable.from(execution).getOptional().ifPresent(xAccessToken ->
        httpHeadersList.add(PlatformHttpHeader.X_ACCESS_TOKEN.getName(), xAccessToken));
    return this;
  }

  public HeaderBuilder contentTypeJson() {
    httpHeadersList.setContentType(MediaType.APPLICATION_JSON);
    return this;
  }

  public HeaderBuilder csrfProtectionHeaders() {
    var requestXsrfToken = UUID.randomUUID().toString();
    httpHeadersList.add(PlatformHttpHeader.X_XSRF_TOKEN.getName(), requestXsrfToken);
    httpHeadersList.add("Cookie", String.format("%s=%s", XSRF_COOKIE_NAME, requestXsrfToken));
    return this;
  }

  public HttpHeaders build() {
    var headers = new HttpHeaders();
    headers.addAll(httpHeadersList);
    httpHeadersList.clear();
    return headers;
  }
}
