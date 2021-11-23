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

package com.epam.digital.data.platform.dataaccessor.initiator;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Base implementation of {@link InitiatorVariablesAccessor} that creates {@link
 * BaseInitiatorVariablesReadWriteAccessor} as implementation of {@link
 * InitiatorVariablesReadAccessor} and {@link InitiatorVariablesWriteAccessor}
 */
@RequiredArgsConstructor
public class BaseInitiatorVariablesAccessor implements InitiatorVariablesAccessor {

  private final VariableAccessorFactory variableAccessorFactory;

  @Override
  public InitiatorVariablesReadAccessor from(DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  @Override
  public InitiatorVariablesWriteAccessor on(DelegateExecution delegateExecution) {
    return accessor(delegateExecution);
  }

  private BaseInitiatorVariablesReadWriteAccessor accessor(DelegateExecution execution) {
    var variableAccessor = variableAccessorFactory.from(execution);
    return new BaseInitiatorVariablesReadWriteAccessor(variableAccessor,
        (ExecutionEntity) execution);
  }
}
