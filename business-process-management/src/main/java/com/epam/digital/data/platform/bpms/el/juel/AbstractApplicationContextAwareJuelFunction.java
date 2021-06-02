package com.epam.digital.data.platform.bpms.el.juel;

import com.epam.digital.data.platform.bpms.el.juel.mapper.CompositeApplicationContextAwareJuelFunctionMapper;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

/**
 * Base class for all custom lowcode JUEL functions. All child of the class will be added to Camunda
 * bindings list for using the function in expression language. Child must have one public static
 * method that will be mapped on JUEL function. <i>The method can be overloaded but only <b>one</b>
 * of these methods (based on parameter types list) can be used from the expression language</i>
 * <p>
 * Implements {@link ApplicationContextAware} for having access to Spring application context from
 * static methods
 *
 * @see CompositeApplicationContextAwareJuelFunctionMapper JUEL Funtion Mapper
 */
@Getter
public abstract class AbstractApplicationContextAwareJuelFunction implements
    ApplicationContextAware {

  private static ApplicationContext applicationContext;

  private final Method juelFunctionMethod;

  /**
   * Base constructor of AbstractApplicationContextAwareJuelFunction
   *
   * @param juelFunctionName name of the static method that has to be mapped on JUEL function that
   *                         will be used in expression language (must equal the real method name)
   * @param paramTypes       array of parameter types of the function
   */
  public AbstractApplicationContextAwareJuelFunction(String juelFunctionName,
      Class<?>... paramTypes) {
    this.juelFunctionMethod = ReflectionUtils.findMethod(getClass(), juelFunctionName, paramTypes);
  }

  protected static CoreExecution getExecution() {
    return Context.getCoreExecutionContext().getExecution();
  }

  protected static <T> T getBean(@NonNull Class<T> beanType) {
    return Objects.requireNonNull(applicationContext, "Spring app context isn't initialized yet")
        .getBean(beanType);
  }

  @Override
  public final void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    AbstractApplicationContextAwareJuelFunction.applicationContext = applicationContext;
  }
}
