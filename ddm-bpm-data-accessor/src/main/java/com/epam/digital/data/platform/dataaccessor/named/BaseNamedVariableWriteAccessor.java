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

package com.epam.digital.data.platform.dataaccessor.named;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Base implementation of {@link NamedVariableWriteAccessor} that is used {@link VariableAccessor}
 * for write access the variable with predefined name
 */
@RequiredArgsConstructor
public class BaseNamedVariableWriteAccessor<T> implements NamedVariableWriteAccessor<T> {

  private final String name;
  private final VariableAccessor accessor;
  private final boolean isTransient;

  @Override
  public void set(@Nullable T value) {
    if (isTransient) {
      accessor.setVariableTransient(name, value);
    } else {
      accessor.setVariable(name, value);
    }
  }

  @Override
  public void setLocal(@Nullable T value) {
    if (isTransient) {
      accessor.setVariableTransient(name, value);
    } else {
      accessor.setVariableLocal(name, value);
    }
  }

  @Override
  public void remove() {
    accessor.removeVariable(name);
  }
}
