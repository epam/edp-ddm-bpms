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

package com.epam.digital.data.platform.bpms.extension.delegate.message;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.google.common.base.Strings;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component(SendMessageDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class SendMessageDelegate extends AbstractMessageDelegate {

  public static final String DELEGATE_NAME = "sendMessageDelegate";

  private final RuntimeService runtimeService;

  @SystemVariable(name = "correlationProcessInstanceId")
  private NamedVariableAccessor<String> correlationProcessInstanceIdVariable;
  @SystemVariable(name = "correlationVariables")
  private NamedVariableAccessor<Map<String, Object>> correlationVariablesVariable;
  @SystemVariable(name = "messageData")
  private NamedVariableAccessor<Map<String, Object>> dataVariable;

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  @Override
  protected void executeInternal(@NonNull DelegateExecution execution) throws Exception {
    var correlationProcessInstanceId = correlationProcessInstanceIdVariable.from(execution).get();
    var correlationVariables = correlationVariablesVariable.from(execution).getOrDefault(Map.of());
    requireCorrelationIdentifiers(correlationProcessInstanceId, correlationVariables);

    var correlationMessage = getMessageName(execution);
    var variableMap = dataVariable.from(execution).getOrDefault(Map.of());
    correlateWithResult(correlationMessage, correlationProcessInstanceId, correlationVariables,
        variableMap);
  }

  private void requireCorrelationIdentifiers(
      @Nullable String correlationProcessInstanceId,
      @NonNull Map<String, Object> correlationVariables) {
    if (Strings.isNullOrEmpty(correlationProcessInstanceId) && correlationVariables.isEmpty()) {
      throw new IllegalArgumentException(
          "Can't correlate message to process due to missing correlation identifiers");
    }
  }

  private void correlateWithResult(
      @NonNull String correlationMessage,
      @Nullable String correlationProcessInstanceId,
      @NonNull Map<String, Object> correlationVariables,
      @NonNull Map<String, Object> data) {

    var builder = runtimeService.createMessageCorrelation(correlationMessage)
        .setVariables(data)
        .processInstanceVariablesEqual(correlationVariables);

    if (Strings.isNullOrEmpty(correlationProcessInstanceId)) {
      builder.correlateAllWithResult();
    } else {
      builder.processInstanceId(correlationProcessInstanceId)
          .correlateWithResult();
    }
  }
}