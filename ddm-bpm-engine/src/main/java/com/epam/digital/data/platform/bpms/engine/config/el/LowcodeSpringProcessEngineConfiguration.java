package com.epam.digital.data.platform.bpms.engine.config.el;

import com.epam.digital.data.platform.bpms.engine.manager.factory.BaseClassManagerFactory;
import com.epam.digital.data.platform.bpms.engine.service.BatchFormService;
import java.util.List;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link SpringProcessEngineConfiguration} that is used
 * for overriding default camunda process engine configuration.
 */
@Component
public class LowcodeSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

  private final BatchFormService formServiceImpl;
  private final List<BaseClassManagerFactory<?>> genericManagerFactories;

  @Autowired
  public LowcodeSpringProcessEngineConfiguration(
      BatchFormService formServiceImpl,
      BpmnParseFactory bpmnParseFactory,
      List<BaseClassManagerFactory<?>> genericManagerFactories) {
    this.formServiceImpl = formServiceImpl;
    this.bpmnParseFactory = bpmnParseFactory;
    this.genericManagerFactories = genericManagerFactories;
  }

  @Override
  public void initServices() {
    super.initServices();
    initService(formServiceImpl);
  }

  @Override
  protected void initSessionFactories() {
    super.initSessionFactories();
    genericManagerFactories.forEach(this::addSessionFactory);
  }
}
