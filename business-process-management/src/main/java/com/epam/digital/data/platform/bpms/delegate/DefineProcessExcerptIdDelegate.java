package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import com.epam.digital.data.platform.starter.logger.annotation.Logging;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to save excerpt id to
 * a business process system variable
 */
@Component("defineProcessExcerptIdDelegate")
@RequiredArgsConstructor
@Logging
public class DefineProcessExcerptIdDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    var excerptId = execution.getVariable("excerptId");
    execution.setVariable(Constants.SYS_VAR_PROCESS_EXCERPT_ID, excerptId);
  }
}
