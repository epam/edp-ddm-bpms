package com.epam.digital.data.platform.bpms.engine.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Configuration;

/**
 * The class extends {@link AbstractProcessEnginePlugin} class and used for enabling additional
 * pre/post parse listeners.
 */
@Configuration
@RequiredArgsConstructor
public class CamundaEngineSystemVariablesSupportListenerPlugin extends AbstractProcessEnginePlugin {

  private final CamundaEngineSystemVariablesSupportListener camundaEngineSystemVariablesSupportListener;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    List<BpmnParseListener> preParseListeners = processEngineConfiguration
        .getCustomPreBPMNParseListeners();

    if (preParseListeners == null) {
      preParseListeners = new ArrayList<>();
      processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
    }
    preParseListeners.add(camundaEngineSystemVariablesSupportListener);
  }
}
