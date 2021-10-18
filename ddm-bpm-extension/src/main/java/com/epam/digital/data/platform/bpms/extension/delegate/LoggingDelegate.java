package com.epam.digital.data.platform.bpms.extension.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used for logging value of
 * content variable
 */
@Slf4j
@Component("loggingDelegate")
@Deprecated
public class LoggingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    log.info((String) execution.getVariable("content"));
  }
}
