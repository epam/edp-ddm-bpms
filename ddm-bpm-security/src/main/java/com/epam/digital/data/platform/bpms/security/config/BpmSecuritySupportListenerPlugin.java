package com.epam.digital.data.platform.bpms.security.config;

import java.util.ArrayList;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Configuration;

/**
 * The class extends {@link AbstractProcessEnginePlugin} class and used for enabling additional
 * pre/post parse listeners.
 */
@Configuration
@RequiredArgsConstructor
public class BpmSecuritySupportListenerPlugin extends AbstractProcessEnginePlugin {

  private final BpmSecuritySupportListener bpmSecuritySupportListener;

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    var preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
    if (Objects.isNull(preParseListeners)) {
      preParseListeners = new ArrayList<>();
      processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
    }
    preParseListeners.add(bpmSecuritySupportListener);
  }
}
