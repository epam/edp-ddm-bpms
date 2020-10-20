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

package com.epam.digital.data.platform.dataaccessor.sysvar;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessor;

/**
 * Named variable accessor for system variable with name {@code sys-var-process-excerpt-id} and type
 * {@link String}
 */
public class ProcessExcerptIdVariable extends BaseNamedVariableAccessor<String> {

  public static final String SYS_VAR_PROCESS_EXCERPT_ID = "sys-var-process-excerpt-id";

  public ProcessExcerptIdVariable(VariableAccessorFactory variableAccessorFactory) {
    super(SYS_VAR_PROCESS_EXCERPT_ID, false, variableAccessorFactory);
  }
}
