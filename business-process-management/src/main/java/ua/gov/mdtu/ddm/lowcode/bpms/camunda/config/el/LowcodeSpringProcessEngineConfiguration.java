package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config.el;

import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Lowcode implementation of SpringProcessEngineConfiguration that is used for overriding default
 * camunda process engine configuration
 */
@Component
public class LowcodeSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

  @Autowired
  public LowcodeSpringProcessEngineConfiguration(BpmnParseFactory bpmnParseFactory) {
    this.bpmnParseFactory = bpmnParseFactory;
  }
}
