package com.epam.digital.data.platform.bpms.rest.dto;

import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Dto that contains business process system variables.
 * <p>
 * Used for accessing such system variables:
 * <li>processCompletionResult - sys-var-process-completion-result</li>
 * <li>excerptId - sys-var-process-excerpt-id</li>
 */
@RequiredArgsConstructor
public class SystemVariablesDto {

  private final Map<String, Object> variables;

  public String getProcessCompletionResult() {
    return (String) variables.get(
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
  }

  public String getExcerptId() {
    return (String) variables.get(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
  }

}
