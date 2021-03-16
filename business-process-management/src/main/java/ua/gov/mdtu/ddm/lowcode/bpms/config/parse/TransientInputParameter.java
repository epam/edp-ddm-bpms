package ua.gov.mdtu.ddm.lowcode.bpms.config.parse;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * TransientInputParameter is used for saving transient flag in setting variable from outer to inner
 * scope
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
    if (!(value instanceof TypedValue) || !((TypedValue) value).isTransient()) {
      innerScope.setVariableLocal(name, value);
    } else {
      innerScope.setVariableLocalTransient(name, value);
    }
  }
}
