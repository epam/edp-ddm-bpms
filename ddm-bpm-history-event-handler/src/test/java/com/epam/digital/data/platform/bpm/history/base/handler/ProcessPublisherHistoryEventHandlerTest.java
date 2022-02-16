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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bphistory.model.HistoryProcess;
import com.epam.digital.data.platform.bphistory.model.HistoryTask;
import com.epam.digital.data.platform.bpm.history.base.mapper.HistoryMapper;
import com.epam.digital.data.platform.bpm.history.base.publisher.ProcessHistoryEventPublisher;
import com.epam.digital.data.platform.bpms.rest.service.repository.ProcessInstanceRuntimeService;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProcessPublisherHistoryEventHandlerTest {

  @Spy
  @InjectMocks
  private ProcessPublisherHistoryEventHandler processPublisherHistoryEventHandler;
  @Mock
  private ProcessHistoryEventPublisher publisher;
  @Spy
  private HistoryMapper historyMapper = Mappers.getMapper(HistoryMapper.class);
  @Mock
  private CamundaImpersonationFactory camundaImpersonationFactory;
  @Mock
  private CamundaImpersonation camundaImpersonation;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ProcessInstanceRuntimeService processInstanceRuntimeService;

  @Captor
  private ArgumentCaptor<HistoryProcess> historyProcessArgumentCaptor;
  @Captor
  private ArgumentCaptor<HistoryTask> historyTaskArgumentCaptor;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(processPublisherHistoryEventHandler, "camundaImpersonationFactory",
        camundaImpersonationFactory);
    ReflectionTestUtils.setField(processPublisherHistoryEventHandler, "repositoryService",
        repositoryService);
    ReflectionTestUtils.setField(processPublisherHistoryEventHandler, "historyMapper", historyMapper);
    ReflectionTestUtils.setField(processPublisherHistoryEventHandler, "processInstanceRuntimeService", processInstanceRuntimeService);
    lenient().when(camundaImpersonationFactory.getCamundaImpersonation())
        .thenReturn(Optional.of(camundaImpersonation));
    lenient().doAnswer(invocation -> {
      Supplier<?> supplier = invocation.getArgument(0);
      return supplier.get();
    }).when(camundaImpersonation).execute(any());

    var processInstance = new ExecutionEntity();
    processInstance.setId("rootProcessInstanceId");
    processInstance.setRootProcessInstanceId("rootProcessInstanceId");
    processInstance.setProcessDefinitionId("processDefinitionId");
    lenient().when(processInstanceRuntimeService.getProcessInstance("rootProcessInstanceId"))
        .thenReturn(Optional.of(processInstance));

    var processDefinition = new ProcessDefinitionEntity();
    processDefinition.setName("processDefinitionName");
    lenient().when(repositoryService.getProcessDefinition("processDefinitionId"))
        .thenReturn(processDefinition);
  }

  @Test
  void handleEvents() {
    processPublisherHistoryEventHandler.handleEvents(null);
    verify(processPublisherHistoryEventHandler, never()).handleEvent(any());

    var event1 = mock(HistoryEvent.class);
    var event2 = mock(HistoryEvent.class);
    processPublisherHistoryEventHandler.handleEvents(List.of(event1, event2));
    verify(processPublisherHistoryEventHandler).handleEvent(event1);
    verify(processPublisherHistoryEventHandler).handleEvent(event2);
  }

  @Test
  void handleProcessInstanceEvent_create() {
    var event = new HistoricProcessInstanceEventEntity();
    event.setEventType(HistoryEventTypes.PROCESS_INSTANCE_START.getEventName());
    event.setProcessInstanceId("processInstanceId");
    event.setSuperProcessInstanceId("superProcessInstanceId");
    event.setProcessDefinitionId("processDefinitionId");
    event.setProcessDefinitionKey("processDefinitionKey");
    event.setBusinessKey("businessKey");
    event.setStartTime(Date.from(LocalDateTime.of(2021, 12, 10, 18, 18).toInstant(ZoneOffset.UTC)));
    event.setStartUserId("startUserId");
    event.setState("state");

    processPublisherHistoryEventHandler.handleEvent(event);

    verify(publisher).put(historyProcessArgumentCaptor.capture());

    var value = historyProcessArgumentCaptor.getValue();
    assertThat(value)
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("superProcessInstanceId", "superProcessInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionKey", "processDefinitionKey")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinitionName")
        .hasFieldOrPropertyWithValue("businessKey", "businessKey")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2021, 12, 10, 18, 18))
        .hasFieldOrPropertyWithValue("endTime", null)
        .hasFieldOrPropertyWithValue("startUserId", "startUserId")
        .hasFieldOrPropertyWithValue("state", "state");
  }

  @Test
  void handleProcessInstanceEvent_end() {
    var event = new HistoricProcessInstanceEventEntity();
    event.setEventType(HistoryEventTypes.PROCESS_INSTANCE_END.getEventName());
    event.setProcessInstanceId("processInstanceId");
    event.setSuperProcessInstanceId("superProcessInstanceId");
    event.setProcessDefinitionId("processDefinitionId");
    event.setProcessDefinitionKey("processDefinitionKey");
    event.setBusinessKey("businessKey");
    event.setEndTime(Date.from(LocalDateTime.of(2021, 12, 10, 18, 22).toInstant(ZoneOffset.UTC)));
    event.setStartUserId("startUserId");
    event.setState("state");

    processPublisherHistoryEventHandler.handleEvent(event);

    verify(publisher).patch(historyProcessArgumentCaptor.capture());

    var value = historyProcessArgumentCaptor.getValue();
    assertThat(value)
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("superProcessInstanceId", "superProcessInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionKey", "processDefinitionKey")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinitionName")
        .hasFieldOrPropertyWithValue("businessKey", "businessKey")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("endTime", LocalDateTime.of(2021, 12, 10, 18, 22))
        .hasFieldOrPropertyWithValue("startUserId", "startUserId")
        .hasFieldOrPropertyWithValue("state", "state");
  }

  @Test
  void handleVariableUpdateEvent_noSystemVariable() {
    var nonSystemVarEvent = new HistoricVariableUpdateEventEntity();
    nonSystemVarEvent.setVariableName("nonSystemName");

    processPublisherHistoryEventHandler.handleEvent(nonSystemVarEvent);

    verify(publisher, never()).put(any(HistoryProcess.class));
    verify(publisher, never()).patch(any(HistoryProcess.class));
    verify(publisher, never()).put(any(HistoryTask.class));
  }

  @Test
  void handleVariableUpdateEvent_deleteSystemVariableEventIgnored() {
    var processCompletionResult = new HistoricVariableUpdateEventEntity();
    processCompletionResult.setProcessInstanceId("processInstanceId");
    processCompletionResult.setVariableName(
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
    processCompletionResult.setTextValue("process completed for a reason");
    processCompletionResult.setEventType(HistoryEventTypes.VARIABLE_INSTANCE_DELETE.getEventName());

    processPublisherHistoryEventHandler.handleEvent(processCompletionResult);

    verify(publisher, never()).put(any(HistoryProcess.class));
    verify(publisher, never()).patch(any(HistoryProcess.class));
    verify(publisher, never()).put(any(HistoryTask.class));
  }

  @Test
  void handleVariableUpdateEvent_completionResult() {
    var processCompletionResult = new HistoricVariableUpdateEventEntity();
    processCompletionResult.setProcessInstanceId("processInstanceId");
    processCompletionResult.setVariableName(
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT);
    processCompletionResult.setTextValue("process completed for a reason");
    processCompletionResult.setEventType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEventName());

    processPublisherHistoryEventHandler.handleEvent(processCompletionResult);
    verify(publisher).patch(historyProcessArgumentCaptor.capture());
    var processCompletionValue = historyProcessArgumentCaptor.getValue();
    assertThat(processCompletionValue)
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("completionResult", "process completed for a reason");
  }

  @Test
  void handleVariableUpdateEvent_excerptId() {
    var excerptId = new HistoricVariableUpdateEventEntity();
    excerptId.setProcessInstanceId("processInstanceId");
    excerptId.setVariableName(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
    excerptId.setTextValue("excerpt");
    excerptId.setEventType(HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEventName());

    processPublisherHistoryEventHandler.handleEvent(excerptId);
    verify(publisher).patch(historyProcessArgumentCaptor.capture());
    var excerptValue = historyProcessArgumentCaptor.getValue();
    assertThat(excerptValue)
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("excerptId", "excerpt");
  }

  @Test
  void handleTaskEvent_create() {
    var taskEvent = new HistoricTaskInstanceEventEntity();
    taskEvent.setEventType(HistoryEventTypes.TASK_INSTANCE_CREATE.getEventName());
    taskEvent.setActivityInstanceId("activityInstanceId");
    taskEvent.setTaskDefinitionKey("taskDefinitionKey");
    taskEvent.setName("taskDefinitionName");
    taskEvent.setProcessInstanceId("processInstanceId");
    taskEvent.setProcessDefinitionId("processDefinitionId");
    taskEvent.setProcessDefinitionKey("processDefinitionKey");
    taskEvent.setRootProcessInstanceId("rootProcessInstanceId");
    taskEvent.setStartTime(
        Date.from(LocalDateTime.of(2021, 12, 10, 18, 18).toInstant(ZoneOffset.UTC)));
    taskEvent.setAssignee("assignee");

    processPublisherHistoryEventHandler.handleEvent(taskEvent);
    verify(publisher).put(historyTaskArgumentCaptor.capture());
    var value = historyTaskArgumentCaptor.getValue();
    assertThat(value)
        .hasFieldOrPropertyWithValue("activityInstanceId", "activityInstanceId")
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "taskDefinitionKey")
        .hasFieldOrPropertyWithValue("taskDefinitionName", "taskDefinitionName")
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionKey", "processDefinitionKey")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinitionName")
        .hasFieldOrPropertyWithValue("rootProcessInstanceId", "rootProcessInstanceId")
        .hasFieldOrPropertyWithValue("startTime", LocalDateTime.of(2021, 12, 10, 18, 18))
        .hasFieldOrPropertyWithValue("endTime", null)
        .hasFieldOrPropertyWithValue("assignee", "assignee");
  }

  @Test
  void handleTaskEvent_complete() {
    var taskEvent = new HistoricTaskInstanceEventEntity();
    taskEvent.setEventType(HistoryEventTypes.TASK_INSTANCE_COMPLETE.getEventName());
    taskEvent.setActivityInstanceId("activityInstanceId");
    taskEvent.setTaskDefinitionKey("taskDefinitionKey");
    taskEvent.setName("taskDefinitionName");
    taskEvent.setProcessInstanceId("processInstanceId");
    taskEvent.setProcessDefinitionId("processDefinitionId");
    taskEvent.setProcessDefinitionKey("processDefinitionKey");
    taskEvent.setRootProcessInstanceId("rootProcessInstanceId");
    taskEvent.setEndTime(
        Date.from(LocalDateTime.of(2021, 12, 10, 18, 22).toInstant(ZoneOffset.UTC)));
    taskEvent.setAssignee("assignee");

    processPublisherHistoryEventHandler.handleEvent(taskEvent);
    verify(publisher).patch(historyTaskArgumentCaptor.capture());
    var value = historyTaskArgumentCaptor.getValue();
    assertThat(value)
        .hasFieldOrPropertyWithValue("activityInstanceId", "activityInstanceId")
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "taskDefinitionKey")
        .hasFieldOrPropertyWithValue("taskDefinitionName", "taskDefinitionName")
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionKey", "processDefinitionKey")
        .hasFieldOrPropertyWithValue("processDefinitionName", "processDefinitionName")
        .hasFieldOrPropertyWithValue("rootProcessInstanceId", "rootProcessInstanceId")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("endTime", LocalDateTime.of(2021, 12, 10, 18, 22))
        .hasFieldOrPropertyWithValue("assignee", "assignee");
  }

  @Test
  void handleTaskEvent_completeWithNoCamundaImpersonation() {
    when(camundaImpersonationFactory.getCamundaImpersonation()).thenReturn(Optional.empty());

    var taskEvent = new HistoricTaskInstanceEventEntity();
    taskEvent.setEventType(HistoryEventTypes.TASK_INSTANCE_COMPLETE.getEventName());
    taskEvent.setActivityInstanceId("activityInstanceId");
    taskEvent.setTaskDefinitionKey("taskDefinitionKey");
    taskEvent.setName("taskDefinitionName");
    taskEvent.setProcessInstanceId("processInstanceId");
    taskEvent.setProcessDefinitionId("processDefinitionId");
    taskEvent.setProcessDefinitionKey("processDefinitionKey");
    taskEvent.setRootProcessInstanceId("rootProcessInstanceId");
    taskEvent.setEndTime(
        Date.from(LocalDateTime.of(2021, 12, 10, 18, 22).toInstant(ZoneOffset.UTC)));
    taskEvent.setAssignee("assignee");

    processPublisherHistoryEventHandler.handleEvent(taskEvent);
    verify(publisher).patch(historyTaskArgumentCaptor.capture());
    var value = historyTaskArgumentCaptor.getValue();
    assertThat(value)
        .hasFieldOrPropertyWithValue("activityInstanceId", "activityInstanceId")
        .hasFieldOrPropertyWithValue("taskDefinitionKey", "taskDefinitionKey")
        .hasFieldOrPropertyWithValue("taskDefinitionName", "taskDefinitionName")
        .hasFieldOrPropertyWithValue("processInstanceId", "processInstanceId")
        .hasFieldOrPropertyWithValue("processDefinitionId", "processDefinitionId")
        .hasFieldOrPropertyWithValue("processDefinitionKey", "processDefinitionKey")
        .hasFieldOrPropertyWithValue("processDefinitionName", null)
        .hasFieldOrPropertyWithValue("rootProcessInstanceId", "rootProcessInstanceId")
        .hasFieldOrPropertyWithValue("startTime", null)
        .hasFieldOrPropertyWithValue("endTime", LocalDateTime.of(2021, 12, 10, 18, 22))
        .hasFieldOrPropertyWithValue("assignee", "assignee");
  }
}
