package com.epam.digital.data.platform.dataaccessor.sysvar;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessor;

public class ProcessCompletionResultVariable extends BaseNamedVariableAccessor<String> {

  public static final String SYS_VAR_PROCESS_COMPLETION_RESULT = "sys-var-process-completion-result";

  public ProcessCompletionResultVariable(VariableAccessorFactory variableAccessorFactory) {
    super(SYS_VAR_PROCESS_COMPLETION_RESULT, false, variableAccessorFactory);
  }
}
