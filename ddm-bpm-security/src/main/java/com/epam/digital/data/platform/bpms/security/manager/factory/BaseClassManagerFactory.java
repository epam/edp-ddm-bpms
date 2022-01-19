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

package com.epam.digital.data.platform.bpms.security.manager.factory;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

/**
 * Manager factory that is used for replacing class with its child in camunda execution context
 *
 * @param <T> base class that has to be replaced by child
 */
public abstract class BaseClassManagerFactory<T extends Session> implements SessionFactory {

  private final Class<T> baseClass;

  protected BaseClassManagerFactory(Class<T> baseClass) {
    this.baseClass = baseClass;
  }

  @Override
  public Class<T> getSessionType() {
    return baseClass;
  }
}
