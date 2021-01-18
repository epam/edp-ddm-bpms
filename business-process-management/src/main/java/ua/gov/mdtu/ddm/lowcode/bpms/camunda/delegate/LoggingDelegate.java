package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loggingDelegate")
public class LoggingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    log.info((String) execution.getVariable("content"));
  }
}
