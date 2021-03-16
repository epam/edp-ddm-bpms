package ua.gov.mdtu.ddm.lowcode.bpms.camunda.bpmn.config;

import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import ua.gov.mdtu.ddm.lowcode.bpms.config.parse.TransientBpmnParseFactory;

public class LowcodeStandaloneProcessEngineConfiguration extends
    StandaloneProcessEngineConfiguration {

  public LowcodeStandaloneProcessEngineConfiguration() {
    this.bpmnParseFactory = new TransientBpmnParseFactory();
  }
}
