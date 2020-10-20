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
