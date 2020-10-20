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

package com.epam.digital.data.platform.bpms.engine.config.parse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.core.variable.mapping.InputParameter;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 * The class represents an implementation of {@link BpmnParse} that is used for overriding default
 * camunda bpmn parsing for saving properties transient flag.
 */
public class TransientBpmnParse extends BpmnParse {

  public TransientBpmnParse(BpmnParser parser) {
    super(parser);
  }

  @Override
  protected void parseActivityInputOutput(Element activityElement, ActivityImpl activity) {
    super.parseActivityInputOutput(activityElement, activity);
    var ioMapping = activity.getIoMapping();
    if (ioMapping == null) {
      return;
    }
    List<InputParameter> inputParameters = ioMapping.getInputParameters().stream()
        .map(inputParameter -> new TransientInputParameter(inputParameter.getName(),
            inputParameter.getValueProvider())).collect(Collectors.toList());
    ioMapping.setInputParameters(inputParameters);
  }

  @Override
  protected void parseUserTaskCustomExtensions(Element taskElement, ActivityImpl activity, TaskDefinition taskDefinition) {
    super.parseUserTaskCustomExtensions(taskElement, activity, taskDefinition);
    String assignee = taskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (Objects.nonNull(assignee) && assignee.isBlank()) {
      taskDefinition.setAssigneeExpression(null);
    }
  }
}
