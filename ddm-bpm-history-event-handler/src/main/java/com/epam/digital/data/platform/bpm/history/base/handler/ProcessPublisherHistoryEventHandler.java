/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpm.history.base.handler;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bpm.history.base.mapper.HistoryMapper;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * A {@link HistoryEventHandler} that handles {@link HistoricProcessInstanceEventEntity}, {@link
 * HistoricVariableUpdateEventEntity} and {@link HistoricTaskInstanceEventEntity} and publishes them
 * using {@link ProcessHistoryEventPublisher}
 */
@RequiredArgsConstructor
public class ProcessPublisherHistoryEventHandler implements HistoryEventHandler {

  private final ProcessHistoryEventPublisher publisher;

  @Autowired
  private HistoryMapper historyMapper;
  @Lazy
  @Autowired
  private RepositoryService repositoryService;
  @Autowired
  private CamundaImpersonationFactory camundaImpersonationFactory;
  @Lazy
  @Autowired
  private ProcessInstanceRuntimeService processInstanceRuntimeService;


  /**
   * Handle list of fired {@link HistoryEvent}
   *
   * @param historyEvents the {@link HistoryEvent} list that is about to be fired
   * @see ProcessPublisherHistoryEventHandler#handleEvent(HistoryEvent)
   */
  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    if (Objects.nonNull(historyEvents)) {
      historyEvents.forEach(this::handleEvent);
    }
  }

  /**
   * Handle fired {@link HistoryEvent}
   *
   * @param historyEvent the {@link HistoryEvent} that is about to be fired
   * @see ProcessPublisherHistoryEventHandler#handleProcessInstanceEvent(HistoricProcessInstanceEventEntity)
   * @see ProcessPublisherHistoryEventHandler#handleVariableUpdateEvent(HistoricVariableUpdateEventEntity)
   * @see ProcessPublisherHistoryEventHandler#handleHistoricTaskEvent(HistoricTaskInstanceEventEntity)
   */
  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    if (historyEvent instanceof HistoricProcessInstanceEventEntity) {
      this.handleProcessInstanceEvent((HistoricProcessInstanceEventEntity) historyEvent);
    } else if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
      this.handleVariableUpdateEvent((HistoricVariableUpdateEventEntity) historyEvent);
    } else if (historyEvent instanceof HistoricTaskInstanceEventEntity) {
      this.handleHistoricTaskEvent((HistoricTaskInstanceEventEntity) historyEvent);
    }
  }

  /**
   * Update historic process instance due to {@link HistoricProcessInstanceEventEntity fased process
   * intstance event}
   *
   * @param processInstanceEvent the process instance event to be saved in storage
   */
  protected void handleProcessInstanceEvent(
      HistoricProcessInstanceEventEntity processInstanceEvent) {
    fillHistoryEventWithProcessDefinitionName(processInstanceEvent);
    var historyDto = historyMapper.toHistoryProcess(processInstanceEvent);

    if (processInstanceEvent.isEventOfType(HistoryEventTypes.PROCESS_INSTANCE_START)) {
      publisher.put(historyDto);
    }

    publisher.patch(historyDto);
  }

  /**
   * Updates {@link HistoryProcess#getCompletionResult() completion result} of the
   * historic process instance in case of facing event of updating {@link
   * ProcessCompletionResultVariable#SYS_VAR_PROCESS_COMPLETION_RESULT} or {@link
   * HistoryProcess#getExcerptId() excerpt id} in case of updating {@link
   * ProcessExcerptIdVariable#SYS_VAR_PROCESS_EXCERPT_ID}
   * <p>
   * Delete variable events are ignored because system variables mustn't be deleted from process
   * instance
   *
   * @param variableUpdateEvent the variable update event to handle
   */
  protected void handleVariableUpdateEvent(HistoricVariableUpdateEventEntity variableUpdateEvent) {
    var variableName = variableUpdateEvent.getVariableName();
    if (!isCompletionResultOrExcerptId(variableName)
        || variableUpdateEvent.isEventOfType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE)) {
      return;
    }
    publisher.patch(historyMapper.toHistoryProcess(variableUpdateEvent));
  }

  /**
   * Update historic task due to {@link HistoricTaskInstanceEventEntity fased task event}
   *
   * @param taskInstanceEvent the task event to be saved in storage
   */
  protected void handleHistoricTaskEvent(HistoricTaskInstanceEventEntity taskInstanceEvent) {
    fillHistoryEventWithProcessDefinitionName(taskInstanceEvent);
    var historyDto = historyMapper.toHistoryTask(taskInstanceEvent);

    if (taskInstanceEvent.isEventOfType(HistoryEventTypes.TASK_INSTANCE_CREATE)) {
      publisher.put(historyDto);
    }

    publisher.patch(historyDto);
  }

  private boolean isCompletionResultOrExcerptId(String variableName) {
    return ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT.equals(variableName)
        || ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID.equals(variableName);
  }

  private void fillHistoryEventWithProcessDefinitionName(HistoryEvent event) {
    var camundaAdminImpersonation = camundaImpersonationFactory.getCamundaImpersonation();

    if (camundaAdminImpersonation.isEmpty()) {
      return;
    }

    var processDefinitionName = camundaAdminImpersonation.get().execute(() ->
        repositoryService.getProcessDefinition(getProcessDefinitionId(event)).getName());

    event.setProcessDefinitionName(processDefinitionName);
  }

  private String getProcessDefinitionId(HistoryEvent event) {
    if (event instanceof HistoricTaskInstanceEventEntity &&
        !event.getProcessInstanceId().equals(event.getRootProcessInstanceId())) {
      var rootProcessInstance = getProcessInstanceOrRoot(
          event.getRootProcessInstanceId());
      return rootProcessInstance.map(ProcessInstance::getProcessDefinitionId)
          .orElseGet(event::getProcessDefinitionId);

    }
    return event.getProcessDefinitionId();
  }

  private Optional<ProcessInstance> getProcessInstanceOrRoot(String processInstanceId) {
    var processInstance = processInstanceRuntimeService.getProcessInstance(processInstanceId);
    return processInstance.map(process -> {
      if (process.getId().equals(process.getRootProcessInstanceId())) {
        return process;
      }
      return processInstanceRuntimeService.getProcessInstanceBySubProcessInstanceId(
          processInstanceId).orElse(null);
    });
  }
}
