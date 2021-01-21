package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;

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
