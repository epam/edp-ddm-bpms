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

package com.epam.digital.data.platform.bpms.engine.config.businesskey;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.bpmn.impl.instance.ActivityImpl;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

/**
 * Implementation of {@link ExecutionListener} that is used for setting business key for business
 * process based on start events extension attributes
 * <p>
 * Performs next steps:
 * <ol>
 * <li>Reads 'businessKeyExpression' start event extension attribute. (It's needed that there are
 * <b>only one</b> such attribute in other case business key won't be set)</li>
 * <li>Creates and resolves expression base on found extension attribute value. (If faced any
 * exception during resolving expression or expression result is more than 255 symbols length
 * business key won't be set)</li>
 * <li>Set the business key using {@link DelegateExecution#setProcessBusinessKey(String)}</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class BusinessKeyExecutionListener implements ExecutionListener {

  private static final String BUSINESS_KEY_EXPRESSION_EXTENSION_ATTRIBUTE = "businessKeyExpression";
  private static final int BUSINESS_KEY_EXPRESSION_MAX_SIZE = 255;

  private final ExpressionManager expressionManager;

  @Override
  public void notify(DelegateExecution execution) {
    getProcessBusinessKeyExpression(execution)
        .map(expr -> evaluateExpression(expr, execution))
        .ifPresent(execution::setProcessBusinessKey);
  }

  private Optional<String> getProcessBusinessKeyExpression(DelegateExecution execution) {
    var businessKeyExpressions = getBusinessKeyExtensionAttributes(execution);

    if (businessKeyExpressions.isEmpty()) {
      return Optional.empty();
    }

    if (businessKeyExpressions.size() > 1) {
      log.info("Found several {} extension properties in {} business process. "
              + "Skipping defining business key...",
          BUSINESS_KEY_EXPRESSION_EXTENSION_ATTRIBUTE, execution.getProcessDefinitionId());
      return Optional.empty();
    }
    return Optional.of(businessKeyExpressions.get(0).getCamundaValue());
  }

  /**
   * Get extension attributes of current event by next chain: {@link ActivityImpl current start
   * event} -> {@link ExtensionElements} -> {@link CamundaProperties} -> collection of {@link
   * CamundaProperty} filtered by name
   *
   * @param execution current execution
   * @return list of extension attributes of current event
   */
  private List<CamundaProperty> getBusinessKeyExtensionAttributes(
      DelegateExecution execution) {
    return execution.getBpmnModelInstance()
        .getModelElementById(execution.getCurrentActivityId())
        .getChildElementsByType(ExtensionElements.class).stream()
        .map(extensionElements -> extensionElements.getChildElementsByType(CamundaProperties.class))
        .flatMap(Collection::stream)
        .map(CamundaProperties::getCamundaProperties)
        .flatMap(Collection::stream)
        .filter(property -> BUSINESS_KEY_EXPRESSION_EXTENSION_ATTRIBUTE
            .equals(property.getCamundaName()))
        .collect(Collectors.toList());
  }

  private String evaluateExpression(String expressionStr, DelegateExecution execution) {
    var expression = expressionManager.createExpression(expressionStr);
    var expressionValue = getExpressionValue(expression, execution);
    if (expressionValue.isEmpty()) {
      return null;
    }

    var stringExpressionValue = expressionValue.get().toString();
    if (stringExpressionValue.length() > BUSINESS_KEY_EXPRESSION_MAX_SIZE) {
      log.info("Business key expression result is too big (more than {} symbols) "
              + "on {} business process. Skipping setting business key...",
          BUSINESS_KEY_EXPRESSION_MAX_SIZE, execution.getProcessDefinitionId());
      return null;
    }
    return stringExpressionValue;
  }

  private Optional<Object> getExpressionValue(Expression expression, DelegateExecution execution) {
    try {
      return Optional.ofNullable(expression.getValue(execution, execution));
    } catch (RuntimeException e) {
      log.info("Couldn't resolve an expression on {} business process. Cause: {}",
          execution.getProcessDefinitionId(), e.getMessage(), e);
      return Optional.empty();
    }
  }
}
