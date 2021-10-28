package com.epam.digital.data.platform.dataaccessor.annotation;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used for defining a Spring bean field with type {@link NamedVariableAccessor}
 * as variable accessor.
 *
 * <pre>
 *   &#64;SystemVariable(name = "variable", isTransient = true)
 *   private FixedVariableAccessor&#60;String&#62; variable;
 *   ...
 *   // and in any method of the class you can use it
 *   variable.on(delegateExecution).set("value");
 * </pre>
 *
 * @implNote variable type <b>has to</b> be {@link NamedVariableAccessor}
 * @see SystemVariableBeanPostProcessor
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemVariable {

  /**
   * Name of the variable
   */
  String name();

  /**
   * Variable transient flag
   */
  boolean isTransient() default false;
}
