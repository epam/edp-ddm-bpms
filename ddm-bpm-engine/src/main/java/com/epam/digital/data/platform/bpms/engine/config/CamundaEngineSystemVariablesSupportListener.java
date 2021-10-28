package com.epam.digital.data.platform.bpms.engine.config;

import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
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
public class CamundaEngineSystemVariablesSupportListener extends AbstractBpmnParseListener {

  private final CamundaProperties systemProperties;
  private final VariableAccessorFactory variableAccessorFactory;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        (ExecutionListener) execution -> {
          var variableAccessor = variableAccessorFactory.from(execution);
          systemProperties.getSystemVariables().forEach(variableAccessor::setVariable);
        });
  }
}
