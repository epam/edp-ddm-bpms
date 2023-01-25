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

package com.epam.digital.data.platform.bpms.engine.config.parse;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.variable.Variables;

/**
 * The class extends {@link InputParameter} class and used for saving transient flag in setting
 * variable from outer to inner scope.
 */
public class TransientInputParameter extends InputParameter {

  public TransientInputParameter(String name, ParameterValueProvider valueProvider) {
    super(name, valueProvider);
  }

  @Override
  protected void execute(AbstractVariableScope innerScope, AbstractVariableScope outerScope) {
    // get value from outer scope
    var value = valueProvider.getValue(outerScope);

    ProcessEngineLogger.CORE_LOGGER
        .debugMappingValueFromOuterScopeToInnerScope(value, outerScope, name, innerScope);

    // set variable in inner scope
    innerScope.setVariableLocal(name, Variables.untypedValue(value, true));
  }
}
