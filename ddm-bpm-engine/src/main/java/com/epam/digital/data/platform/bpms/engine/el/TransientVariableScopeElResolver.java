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

package com.epam.digital.data.platform.bpms.engine.el;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.el.VariableScopeElResolver;
import org.camunda.bpm.engine.impl.javax.el.ELContext;

/**
 * The class represents an implementation of {@link VariableScopeElResolver} that is used for
 * setting transient scope variable.
 */
public class TransientVariableScopeElResolver extends VariableScopeElResolver {

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base != null) {
      return null;
    }

    var scopeContext = context.getContext(VariableScope.class);
    if (scopeContext == null) {
      return null;
    }

    var scope = (AbstractVariableScope) scopeContext;
    var variableName = (String) property;
    if (!scope.hasVariable(variableName)) {
      return null;
    }

    context.setPropertyResolved(true);
    var typedValue = scope.getVariableTyped(variableName);
    if (!typedValue.isTransient()) {
      return scope.getVariable(variableName);
    }
    return typedValue;
  }
}
