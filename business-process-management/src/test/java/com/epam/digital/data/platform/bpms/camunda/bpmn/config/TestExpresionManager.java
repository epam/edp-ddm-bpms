package com.epam.digital.data.platform.bpms.camunda.bpmn.config;

import com.epam.digital.data.platform.bpms.config.el.TransientVariableScopeElResolver;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

public class TestExpresionManager extends ExpressionManager {

  @Override
  protected ELResolver createElResolver() {
    var superResolver = super.createElResolver();

    var resolver = new CompositeELResolver();
    resolver.add(new TransientVariableScopeElResolver());
    resolver.add(superResolver);

    return resolver;
  }
}
