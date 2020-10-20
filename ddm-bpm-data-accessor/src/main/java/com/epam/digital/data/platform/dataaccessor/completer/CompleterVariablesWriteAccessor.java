/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
