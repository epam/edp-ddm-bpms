package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.errorhandling.BaseRestExceptionHandler;
import ua.gov.mdtu.ddm.general.errorhandling.exception.SystemException;

/**
 * Throws user data validation exception with details based on user input.
 */
@Component("camundaSystemErrorDelegate")
@RequiredArgsConstructor
public class CamundaSystemErrorDelegate implements JavaDelegate {

  private static final String VAR_SYSTEM_ERROR = "systemError";

  @Override
  public void execute(DelegateExecution execution) {
    var systemError = execution.hasVariable(VAR_SYSTEM_ERROR) ?
        (String) execution.getVariable(VAR_SYSTEM_ERROR) : StringUtils.EMPTY;

    throw new SystemException(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY), "SYSTEM_EXCEPTION",
        "System error", systemError);
  }
}
