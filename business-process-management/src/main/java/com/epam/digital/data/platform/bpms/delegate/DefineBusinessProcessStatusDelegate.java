package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
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

  @Override
  public void execute(DelegateExecution execution) {
    var status = execution.getVariable("status");
    execution.setVariable(Constants.SYS_VAR_PROCESS_COMPLETION_RESULT, status);
  }
}
