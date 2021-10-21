package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.listener.CompleterTaskEventListener;
import com.epam.digital.data.platform.bpms.listener.FileCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.listener.FormDataCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.listener.PutFormDataToCephTaskListener;
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
public class CamundaSystemVariablesSupportListener extends AbstractBpmnParseListener {

  private final CamundaProperties systemProperties;
  private final CompleterTaskEventListener completerTaskEventListener;
  private final PutFormDataToCephTaskListener putFormDataToCephTaskListener;
  private final FileCleanerEndEventListener fileCleanerEndEventListener;
  private final FormDataCleanerEndEventListener formDataCleanerEndEventListener;

  @Override
  public void parseStartEvent(Element startEventElement, ScopeImpl scope,
      ActivityImpl startEventActivity) {
    startEventActivity.addListener(ExecutionListener.EVENTNAME_START,
        (ExecutionListener) execution ->
            systemProperties.getSystemVariables().forEach(execution::setVariable));
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    var userTaskActivityBehavior = ((UserTaskActivityBehavior) activity.getActivityBehavior());
    var taskDefinition = userTaskActivityBehavior.getTaskDefinition();
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, completerTaskEventListener);
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, putFormDataToCephTaskListener);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl endActivity) {
    endActivity.addListener(ExecutionListener.EVENTNAME_END, fileCleanerEndEventListener);
    endActivity.addListener(ExecutionListener.EVENTNAME_END, formDataCleanerEndEventListener);
  }
}
