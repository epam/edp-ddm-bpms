package com.epam.digital.data.platform.dataaccessor.sysvar;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessor;

public class ProcessExcerptIdVariable extends BaseNamedVariableAccessor<String> {

  public static final String SYS_VAR_PROCESS_EXCERPT_ID = "sys-var-process-excerpt-id";

  public ProcessExcerptIdVariable(VariableAccessorFactory variableAccessorFactory) {
    super(SYS_VAR_PROCESS_EXCERPT_ID, false, variableAccessorFactory);
  }
}
