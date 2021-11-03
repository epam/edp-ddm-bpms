package com.epam.digital.data.platform.bpms.config.el;

import com.epam.digital.data.platform.bpms.service.BatchFormService;
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

  @Autowired
  public LowcodeSpringProcessEngineConfiguration(
      BatchFormService formServiceImpl,
      BpmnParseFactory bpmnParseFactory) {
    this.formServiceImpl = formServiceImpl;
    this.bpmnParseFactory = bpmnParseFactory;
  }

  @Override
  public void initServices() {
    super.initServices();
    initService(formServiceImpl);
  }
}
