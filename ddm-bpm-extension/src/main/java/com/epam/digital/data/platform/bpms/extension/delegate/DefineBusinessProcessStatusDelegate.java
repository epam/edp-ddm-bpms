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

package com.epam.digital.data.platform.bpms.extension.delegate;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to define the status
 * of a business process
 */
@Component(DefineBusinessProcessStatusDelegate.DELEGATE_EXECUTION)
@RequiredArgsConstructor
public class DefineBusinessProcessStatusDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "defineBusinessProcessStatusDelegate";

  @SystemVariable(name = "status")
  private NamedVariableAccessor<String> statusVariable;
  private final ProcessCompletionResultVariable sysVarCompletionResult;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var status = statusVariable.from(execution).get();
    sysVarCompletionResult.on(execution).set(status);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
