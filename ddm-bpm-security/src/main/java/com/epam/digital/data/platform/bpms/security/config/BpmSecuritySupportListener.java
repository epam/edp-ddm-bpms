package com.epam.digital.data.platform.bpms.security.config;

import com.epam.digital.data.platform.bpms.security.listener.AuthorizationStartEventListener;
import com.epam.digital.data.platform.bpms.security.listener.CompleterTaskEventListener;
import com.epam.digital.data.platform.bpms.security.listener.InitiatorTokenStartEventListener;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
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
public class BpmSecuritySupportListener extends AbstractBpmnParseListener {

  private final AuthorizationStartEventListener authorizationStartEventListener;
  private final InitiatorTokenStartEventListener initiatorTokenStartEventListener;
  private final CompleterTaskEventListener completerTaskEventListener;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        authorizationStartEventListener);
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        initiatorTokenStartEventListener);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    var userTaskActivityBehavior = ((UserTaskActivityBehavior) activity.getActivityBehavior());
    var taskDefinition = userTaskActivityBehavior.getTaskDefinition();
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, completerTaskEventListener);
  }
}
