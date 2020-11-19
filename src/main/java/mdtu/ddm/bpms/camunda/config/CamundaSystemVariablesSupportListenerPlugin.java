package mdtu.ddm.bpms.camunda.config;


import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Configuration;

/**
 * Plugin that enables additional pre/post parse listeners.
 */
@Configuration
@RequiredArgsConstructor
public class CamundaSystemVariablesSupportListenerPlugin extends AbstractProcessEnginePlugin {

    private final CamundaSystemVariablesSupportListener camundaSystemVariablesSupportListener;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // get all existing preParseListeners
        List<BpmnParseListener> preParseListeners = processEngineConfiguration
                .getCustomPreBPMNParseListeners();

        if (preParseListeners == null) {
            // if no preParseListener exists, create new list
            preParseListeners = new ArrayList<>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        // add new BPMN Parse Listener
        preParseListeners.add(camundaSystemVariablesSupportListener);
    }
}
