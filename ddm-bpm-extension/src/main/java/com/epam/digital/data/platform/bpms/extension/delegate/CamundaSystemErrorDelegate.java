package com.epam.digital.data.platform.bpms.extension.delegate;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
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

  @SystemVariable(name = "systemError")
  private NamedVariableAccessor<String> systemErrorVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var systemError = systemErrorVariable.from(execution).getOrDefault(StringUtils.EMPTY);
    throw new SystemException(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY), "SYSTEM_EXCEPTION",
        "System error", systemError);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
