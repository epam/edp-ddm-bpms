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
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessorFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * {@link BeanPostProcessor} that is used for defining {@link SystemVariable} annotated fields in
 * spring beans.
 *
 * @see SystemVariable
 */
@RequiredArgsConstructor
public class SystemVariableBeanPostProcessor implements BeanPostProcessor {

  private final NamedVariableAccessorFactory namedVariableAccessorFactory;

  @Override
  @Nullable
  public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
      throws BeansException {
    var beanFields = getAllAnnotatedDeclaredFields(bean);
    for (var field : beanFields) {
      ReflectionUtils.makeAccessible(field);
      var value = ReflectionUtils.getField(field, bean);
      if (value != null) {
        continue;
      }

      var annotation = field.getAnnotation(SystemVariable.class);

      var name = annotation.name();
      var isTransient = annotation.isTransient();
      var fixedVariable = namedVariableAccessorFactory.variableAccessor(name, isTransient);

      ReflectionUtils.setField(field, bean, fixedVariable);
    }

    return bean;
  }

  private List<Field> getAllAnnotatedDeclaredFields(Object bean) {
    var resultList = new ArrayList<Field>();

    ReflectionUtils.doWithFields(bean.getClass(), field -> {
      if (field.getType().equals(NamedVariableAccessor.class)
          && field.isAnnotationPresent(SystemVariable.class)) {
        resultList.add(field);
      }
    });

    return resultList;
  }
}
