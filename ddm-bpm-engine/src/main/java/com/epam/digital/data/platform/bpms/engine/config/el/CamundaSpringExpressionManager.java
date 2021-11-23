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

package com.epam.digital.data.platform.bpms.engine.config.el;

import com.epam.digital.data.platform.bpms.engine.el.TransientVariableScopeElResolver;
import java.util.Map;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.springframework.context.ApplicationContext;

/**
 * The class represents an implementation of {@link SpringExpressionManager} that is used for adding
 * custom {@link TransientVariableScopeElResolver} resolver.
 */
public class CamundaSpringExpressionManager extends SpringExpressionManager {

  public CamundaSpringExpressionManager(ApplicationContext applicationContext,
      Map<Object, Object> beans) {
    super(applicationContext, beans);
  }

  @Override
  protected ELResolver createElResolver() {
    var superResolver = super.createElResolver();

    var resolver = new CompositeELResolver();
    resolver.add(new TransientVariableScopeElResolver());
    resolver.add(superResolver);

    return resolver;
  }
}
