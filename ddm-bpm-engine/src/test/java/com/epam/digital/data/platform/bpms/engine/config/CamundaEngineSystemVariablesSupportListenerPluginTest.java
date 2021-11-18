package com.epam.digital.data.platform.bpms.engine.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.commons.compress.utils.Lists;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamundaEngineSystemVariablesSupportListenerPluginTest {

  @InjectMocks
  private CamundaEngineSystemVariablesSupportListenerPlugin camundaSystemVariablesSupportListenerPlugin;
  @Mock
  private CamundaEngineSystemVariablesSupportListener camundaSystemVariablesSupportListener;
  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Test
  void testPreInit() {
    when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);

    camundaSystemVariablesSupportListenerPlugin.preInit(processEngineConfiguration);

    List<BpmnParseListener> listeners = Lists.newArrayList();
    listeners.add(camundaSystemVariablesSupportListener);
    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(listeners);
  }
}
