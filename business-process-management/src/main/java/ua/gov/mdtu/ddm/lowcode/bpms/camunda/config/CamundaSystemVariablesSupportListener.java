package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.stereotype.Component;

/**
 * Bpmn parser listener that adds additional execution listeners to elements.
 */
@Component
@RequiredArgsConstructor
public class CamundaSystemVariablesSupportListener extends AbstractBpmnParseListener {

  private final CamundaProperties systemProperties;
  private final AuthorizationStartEventListener authorizationStartEventListener;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        (ExecutionListener) execution ->
            systemProperties.getSystemVariables().forEach(execution::setVariable));
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        authorizationStartEventListener);
  }
}
