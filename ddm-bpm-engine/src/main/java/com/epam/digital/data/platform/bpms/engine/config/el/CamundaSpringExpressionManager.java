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
