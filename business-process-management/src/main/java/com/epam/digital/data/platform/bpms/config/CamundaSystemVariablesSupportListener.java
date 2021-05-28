package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.listener.AuthorizationStartEventListener;
import com.epam.digital.data.platform.bpms.listener.InitiatorTokenStartEventListener;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.stereotype.Component;

/**
 * The class extends {@link AbstractBpmnParseListener} class and used for adding additional
 * execution listeners to elements.
 */
@Component
@RequiredArgsConstructor
public class CamundaSystemVariablesSupportListener extends AbstractBpmnParseListener {

  private final CamundaProperties systemProperties;
  private final AuthorizationStartEventListener authorizationStartEventListener;
  private final InitiatorTokenStartEventListener initiatorTokenStartEventListener;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        (ExecutionListener) execution ->
            systemProperties.getSystemVariables().forEach(execution::setVariable));
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        authorizationStartEventListener);
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        initiatorTokenStartEventListener);
  }
}
