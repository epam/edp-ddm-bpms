package com.epam.digital.data.platform.bpms.camunda.bpmn.config;

import com.epam.digital.data.platform.bpms.config.parse.TransientBpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class LowcodeStandaloneProcessEngineConfiguration extends
    StandaloneProcessEngineConfiguration {

  public LowcodeStandaloneProcessEngineConfiguration() {
    this.bpmnParseFactory = new TransientBpmnParseFactory();
  }
}
