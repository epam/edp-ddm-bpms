package com.epam.digital.data.platform.bpms.security.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.List;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BpmSecuritySupportListenerPluginTest {

  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  @Mock
  private BpmSecuritySupportListener bpmSecuritySupportListener;

  private BpmSecuritySupportListenerPlugin bpmSecuritySupportListenerPlugin;

  @Before
  public void init() {
    bpmSecuritySupportListenerPlugin = new BpmSecuritySupportListenerPlugin(
        bpmSecuritySupportListener);
  }

  @Test
  public void testPreInit() {
    when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);

    bpmSecuritySupportListenerPlugin.preInit(processEngineConfiguration);

    List<BpmnParseListener> listeners = Lists.newArrayList();
    listeners.add(bpmSecuritySupportListener);
    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(listeners);
  }
}