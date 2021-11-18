package com.epam.digital.data.platform.bpms.engine.manager.factory;

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
