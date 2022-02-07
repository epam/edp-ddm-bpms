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

package com.epam.digital.data.platform.bpms.extension.delegate.message;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.extension.exception.CamundaMessageException;
import java.util.Objects;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.springframework.lang.NonNull;

/**
 * Base class that is used for common messaging functions and variables
 */
public abstract class AbstractMessageDelegate extends BaseJavaDelegate {

  private static final String MESSAGE_REF_BPMN_MODEL_ATTRIBUTE = "messageRef";
  private static final String MESSAGE_NAME_BPMN_MODEL_ATTRIBUTE = "name";

  protected String getMessageName(@NonNull DelegateExecution execution) {
    var bpmnModel = execution.getBpmnModelInstance();
    var currentActivity = bpmnModel.getModelElementById(execution.getCurrentActivityId());
    var messageEventDefinition = currentActivity.getUniqueChildElementByType(
        MessageEventDefinition.class);

    if (Objects.isNull(messageEventDefinition)) {
      throw new CamundaMessageException(
          String.format("Delegate %s has to be used only in message events", getDelegateName()));
    }

    var messageRefId = messageEventDefinition.getAttributeValue(MESSAGE_REF_BPMN_MODEL_ATTRIBUTE);
    var message = bpmnModel.getModelElementById(messageRefId);
    return message.getAttributeValue(MESSAGE_NAME_BPMN_MODEL_ATTRIBUTE);
  }
}
