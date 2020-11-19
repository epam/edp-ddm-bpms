package mdtu.ddm.bpms.camunda.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class CamundaSystemVariablesSupportListenerPluginTest {

    @Mock
    private CamundaSystemVariablesSupportListener camundaSystemVariablesSupportListener;
    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    private CamundaSystemVariablesSupportListenerPlugin camundaSystemVariablesSupportListenerPlugin;

    @Before
    public void init() {
        camundaSystemVariablesSupportListenerPlugin = new CamundaSystemVariablesSupportListenerPlugin(
                camundaSystemVariablesSupportListener);
    }

    @Test
    public void testPreInit() {
        //given
        when(processEngineConfiguration.getCustomPreBPMNParseListeners()).thenReturn(null);
        //when
        camundaSystemVariablesSupportListenerPlugin.preInit(processEngineConfiguration);
        //then
        List<BpmnParseListener> listeners = Lists.newArrayList();
        listeners.add(camundaSystemVariablesSupportListener);
        verify(processEngineConfiguration).setCustomPreBPMNParseListeners(listeners);
    }

}