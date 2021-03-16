package ua.gov.mdtu.ddm.lowcode.bpms.config.el;

import java.util.Map;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.springframework.context.ApplicationContext;

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
