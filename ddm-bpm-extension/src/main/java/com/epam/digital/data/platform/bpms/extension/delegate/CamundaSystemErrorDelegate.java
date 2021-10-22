package com.epam.digital.data.platform.bpms.extension.delegate;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to throw a camunda
 * system exception.
 */
@Component(CamundaSystemErrorDelegate.DELEGATE_EXECUTION)
public class CamundaSystemErrorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "camundaSystemErrorDelegate";
  private static final String VAR_SYSTEM_ERROR = "systemError";

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var systemError = execution.hasVariable(VAR_SYSTEM_ERROR) ?
        (String) execution.getVariable(VAR_SYSTEM_ERROR) : StringUtils.EMPTY;
    try {
      throw new SystemException(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY), "SYSTEM_EXCEPTION",
          "System error", systemError);
    } finally {
      logDelegateExecution(execution, Set.of(VAR_SYSTEM_ERROR), Set.of());
    }
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
