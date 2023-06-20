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

package com.epam.digital.data.platform.bpms.storage.config;

import com.epam.digital.data.platform.bpms.storage.listener.FileCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.storage.listener.FormDataCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.storage.listener.MessagePayloadCleanerEndEventListener;
import com.epam.digital.data.platform.bpms.storage.listener.PutFormDataToStorageTaskListener;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class extends {@link AbstractBpmnParseListener} class and used for adding additional
 * execution listeners to elements.
 */
@Component
@RequiredArgsConstructor
public class StorageBpmnParseListener extends AbstractBpmnParseListener {

  private final PutFormDataToStorageTaskListener putFormDataToStorageTaskListener;
  private final FileCleanerEndEventListener fileCleanerEndEventListener;
  private final FormDataCleanerEndEventListener formDataCleanerEndEventListener;
  private final MessagePayloadCleanerEndEventListener messagePayloadCleanerEndEventListener;
  @Value("${storage.form-data.cleaningEndEventEnabled:true}")
  private final boolean isEnabledFormDataCleaningEndEvent;

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    var userTaskActivityBehavior = ((UserTaskActivityBehavior) activity.getActivityBehavior());
    var taskDefinition = userTaskActivityBehavior.getTaskDefinition();
    taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, putFormDataToStorageTaskListener);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl endActivity) {
    if (scope instanceof ProcessDefinitionImpl) {
      endActivity.addBuiltInListener(ExecutionListener.EVENTNAME_END, fileCleanerEndEventListener);
      if (isEnabledFormDataCleaningEndEvent) {
        endActivity.addBuiltInListener(ExecutionListener.EVENTNAME_END,
                formDataCleanerEndEventListener);
      }
      endActivity.addBuiltInListener(ExecutionListener.EVENTNAME_END,
          messagePayloadCleanerEndEventListener);
    }
  }
}
