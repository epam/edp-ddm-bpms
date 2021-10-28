package com.epam.digital.data.platform.dataaccessor.completer;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Class that is used for <i>completer</i> variables write only accessing
 */
public interface CompleterVariablesWriteAccessor {

  /**
   * Set task completer to a process variable
   *
   * @param taskDefinitionKey task definition key of the completed task
   * @param completerName     name of the user that completed the task
   */
  void setTaskCompleter(@NonNull String taskDefinitionKey, @Nullable String completerName);

  /**
   * Set task completer token to a transient process variable
   *
   * @param taskDefinitionKey task definition key of the completed task
   * @param completerToken    token of the user that completed the task
   */
  void setTaskCompleterToken(@NonNull String taskDefinitionKey, @Nullable String completerToken);
}
