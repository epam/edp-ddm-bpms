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
    logStartDelegateExecution();
    var status = statusVariable.from(execution).get();
    sysVarCompletionResult.on(execution).set(status);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
