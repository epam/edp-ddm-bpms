package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to define the status
 * of a business process
 */
@Component("defineBusinessProcessStatusDelegate")
@RequiredArgsConstructor
@Logging
public class DefineBusinessProcessStatusDelegate implements JavaDelegate {

  private static final String SYS_VAR_PROCESS_COMPLETION_RESULT = "sys-var-process-completion-result";

  @Override
  public void execute(DelegateExecution execution) {
    var status = execution.getVariable("status");
    execution.setVariable(SYS_VAR_PROCESS_COMPLETION_RESULT, status);
  }
}
