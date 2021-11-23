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
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to throw a camunda
 * system exception.
 */
@Component(CamundaSystemErrorDelegate.DELEGATE_EXECUTION)
public class CamundaSystemErrorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "camundaSystemErrorDelegate";

  @SystemVariable(name = "systemError")
  private NamedVariableAccessor<String> systemErrorVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var systemError = systemErrorVariable.from(execution).getOrDefault(StringUtils.EMPTY);
    throw new SystemException(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY), "SYSTEM_EXCEPTION",
        "System error", systemError);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
