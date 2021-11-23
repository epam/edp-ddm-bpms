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

package com.epam.digital.data.platform.dataaccessor.initiator;

import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * Class that is used for <i>initiator</i> variables read only accessing
 */
public interface InitiatorVariablesReadAccessor {

  /**
   * Get process instance initiator from a process variable
   *
   * @return name of the user that initiated the process instance
   */
  @NonNull
  Optional<String> getInitiatorName();

  /**
   * Get initiator token from a process variable
   *
   * @return token of the user that initiated the process instance
   */
  @NonNull
  Optional<String> getInitiatorAccessToken();
}
