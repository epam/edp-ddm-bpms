package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to define the status
 * of a business process
 */
@Component(DefineBusinessProcessStatusDelegate.DELEGATE_EXECUTION)
public class DefineBusinessProcessStatusDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "defineBusinessProcessStatusDelegate";
  public static final String STATUS_PARAMETER = "status";

  @Override
  public void execute(DelegateExecution execution) {
    var status = execution.getVariable(STATUS_PARAMETER);
    setResult(execution, Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, status);
    logDelegateExecution(execution, Set.of(STATUS_PARAMETER), Set.of());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
