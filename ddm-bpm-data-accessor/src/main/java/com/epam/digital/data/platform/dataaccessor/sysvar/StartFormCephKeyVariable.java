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
 * Named variable accessor for system variable with name {@code start_form_ceph_key} and type {@link
 * String}
 */
public class StartFormCephKeyVariable extends BaseNamedVariableAccessor<String> {

  public static final String START_FORM_CEPH_KEY_VARIABLE_NAME = "start_form_ceph_key";

  public StartFormCephKeyVariable(VariableAccessorFactory variableAccessorFactory) {
    super(START_FORM_CEPH_KEY_VARIABLE_NAME, false, variableAccessorFactory);
  }
}
