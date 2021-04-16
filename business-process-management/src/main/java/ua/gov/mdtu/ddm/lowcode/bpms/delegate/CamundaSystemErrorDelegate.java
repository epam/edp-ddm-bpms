package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to throw a camunda
 * system exception.
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
