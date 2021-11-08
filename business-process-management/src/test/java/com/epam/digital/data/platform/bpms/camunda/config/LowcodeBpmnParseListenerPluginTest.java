package com.epam.digital.data.platform.bpms.camunda.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.config.LowcodeBpmnParseListener;
import com.epam.digital.data.platform.bpms.config.LowcodeProcessEnginePlugin;
import java.util.List;
import org.apache.commons.compress.utils.Lists;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LowcodeBpmnParseListenerPluginTest {

  @Mock
  private LowcodeBpmnParseListener lowcodeBpmnParseListener;
  @Mock
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  private LowcodeProcessEnginePlugin lowcodeProcessEnginePlugin;

  @Before
  public void init() {
    lowcodeProcessEnginePlugin = new LowcodeProcessEnginePlugin(
        lowcodeBpmnParseListener);
  }

  @Test
  public void testPreInit() {
    when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);

    lowcodeProcessEnginePlugin.preInit(processEngineConfiguration);

    List<BpmnParseListener> listeners = Lists.newArrayList();
    listeners.add(lowcodeBpmnParseListener);
    verify(processEngineConfiguration).setCustomPreBPMNParseListeners(listeners);
  }

}