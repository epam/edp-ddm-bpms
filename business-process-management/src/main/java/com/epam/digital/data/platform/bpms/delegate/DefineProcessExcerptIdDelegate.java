package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.bpms.api.constant.Constants;
import java.util.Set;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to save excerpt id to
 * a business process system variable
 */
@Component(DefineProcessExcerptIdDelegate.DELEGATE_EXECUTION)
public class DefineProcessExcerptIdDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "defineProcessExcerptIdDelegate";

  @Override
  public void execute(DelegateExecution execution) {
    var excerptId = execution.getVariable("excerptId");
    setResult(execution, Constants.SYS_VAR_PROCESS_EXCERPT_ID, excerptId);
    logDelegateExecution(execution, Set.of(Constants.SYS_VAR_PROCESS_EXCERPT_ID), Set.of());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
