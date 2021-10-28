package com.epam.digital.data.platform.bpms.extension.delegate;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to save excerpt id to
 * a business process system variable
 */
@Component(DefineProcessExcerptIdDelegate.DELEGATE_EXECUTION)
@RequiredArgsConstructor
public class DefineProcessExcerptIdDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_EXECUTION = "defineProcessExcerptIdDelegate";
  public static final String EXCERPT_ID_PARAMETER = "excerptId";

  @SystemVariable(name = "excerptId")
  private NamedVariableAccessor<String> excerptIdVariable;
  private final ProcessExcerptIdVariable sysVarExcerptIdVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var excerptId = excerptIdVariable.from(execution).get();
    sysVarExcerptIdVariable.on(execution).set(excerptId);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_EXECUTION;
  }
}
