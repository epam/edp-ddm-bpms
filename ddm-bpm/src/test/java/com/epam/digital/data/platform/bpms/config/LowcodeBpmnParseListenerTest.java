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

package com.epam.digital.data.platform.bpms.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.config.LowcodeBpmnParseListener;
import com.epam.digital.data.platform.bpms.listener.FileCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.listener.FormDataCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.listener.PutFormDataToCephTaskListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LowcodeBpmnParseListenerTest {

  @Mock
  private ActivityImpl activity;
  @Mock
  private PutFormDataToCephTaskListener putFormDataToCephTaskListener;
  @Mock
  private UserTaskActivityBehavior userTaskActivityBehavior;
  @Mock
  private FileCleanerEndEventListener fileCleanerEndEventListener;
  @Mock
  private FormDataCleanerEndEventListener formDataCleanerEndEventListener;
  @Mock
  private TaskDefinition taskDefinition;

  private LowcodeBpmnParseListener lowcodeBpmnParseListener;

  @Before
  public void init() {
    lowcodeBpmnParseListener = new LowcodeBpmnParseListener(putFormDataToCephTaskListener,
        fileCleanerEndEventListener,
        formDataCleanerEndEventListener);
  }

  @Test
  public void shouldAddTaskListener() {
    when(activity.getActivityBehavior()).thenReturn(userTaskActivityBehavior);
    when(userTaskActivityBehavior.getTaskDefinition()).thenReturn(taskDefinition);

    lowcodeBpmnParseListener.parseUserTask(null, null, activity);

    ArgumentCaptor<TaskListener> captor = ArgumentCaptor.forClass(TaskListener.class);
    verify(taskDefinition, times(1))
        .addTaskListener(eq(TaskListener.EVENTNAME_CREATE), captor.capture());
  }

  @Test
  public void shouldAddEndEventListener() {
    lowcodeBpmnParseListener.parseEndEvent(null, null, activity);

    ArgumentCaptor<ExecutionListener> captor = ArgumentCaptor.forClass(ExecutionListener.class);
    verify(activity, times(2))
        .addListener(eq(ExecutionListener.EVENTNAME_END), captor.capture());
  }
}