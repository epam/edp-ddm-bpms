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

package com.epam.digital.data.platform.bpms.extension.delegate.businesskey;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to define business
 * key of current business process
 */
@Slf4j
@Component(DefineProcessBusinessKeyDelegate.DELEGATE_NAME)
public class DefineProcessBusinessKeyDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "defineProcessBusinessKeyDelegate";
  private static final int BUSINESS_KEY_EXPRESSION_MAX_SIZE = 255;

  @SystemVariable(name = "businessKey")
  private NamedVariableAccessor<String> businessKey;

  @Override
  protected void executeInternal(DelegateExecution execution) {
    var businessKeyValue = businessKey.from(execution).get();

    if (Objects.nonNull(businessKeyValue) &&
        businessKeyValue.length() > BUSINESS_KEY_EXPRESSION_MAX_SIZE) {
      log.info("Business key is too big (more than {} symbols) on {} business process. "
              + "Skipping setting business key...", BUSINESS_KEY_EXPRESSION_MAX_SIZE,
          execution.getProcessDefinitionId());
      return;
    }

    execution.setProcessBusinessKey(businessKeyValue);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
