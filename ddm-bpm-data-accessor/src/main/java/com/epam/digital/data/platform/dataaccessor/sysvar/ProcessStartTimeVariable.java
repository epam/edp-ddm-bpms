package com.epam.digital.data.platform.dataaccessor.sysvar;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessor;
import java.time.LocalDateTime;

/**
 * Named variable accessor for system variable with name {@code sys-var-process-start-time} and type
 * {@link LocalDateTime}
 */
public class ProcessStartTimeVariable extends BaseNamedVariableAccessor<LocalDateTime> {

  public static final String SYS_VAR_PROCESS_START_TIME = "sys-var-process-start-time";

  public ProcessStartTimeVariable(VariableAccessorFactory variableAccessorFactory) {
    super(SYS_VAR_PROCESS_START_TIME, false, variableAccessorFactory);
  }
}
