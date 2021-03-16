package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn.config;

import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import ua.gov.mdtu.ddm.lowcode.bpms.config.el.TransientVariableScopeElResolver;

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
