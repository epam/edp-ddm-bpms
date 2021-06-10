package com.epam.digital.data.platform.bpms.camunda.bpmn.config;

import com.epam.digital.data.platform.bpms.listener.InitiatorTokenStartEventListener;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

@RequiredArgsConstructor
public class TestBpmnParseListener extends AbstractBpmnParseListener {

  private final InitiatorTokenStartEventListener initiatorTokenStartEventListener;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        initiatorTokenStartEventListener);
  }
}
