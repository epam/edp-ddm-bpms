package com.epam.digital.data.platform.dataaccessor.completer;

import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * Class that is used for <i>completer</i> variables read only accessing
 */
public interface CompleterVariablesReadAccessor {

  /**
   * Get task completer from a process variable
   *
   * @param taskDefinitionKey task definition key of the completed task
   * @return name of the user that completed the task
   */
  @NonNull
  Optional<String> getTaskCompleter(@NonNull String taskDefinitionKey);

  /**
   * Get task completer token from a process variable
   *
   * @param taskDefinitionKey task definition key of the completed task
   * @return token of the user that completed the task
   */
  @NonNull
  Optional<String> getTaskCompleterToken(@NonNull String taskDefinitionKey);
}
