package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config.el;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.el.VariableScopeElResolver;
import org.camunda.bpm.engine.impl.javax.el.ELContext;

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
