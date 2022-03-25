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

package com.epam.digital.data.platform.bpms.rest.cmd;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DelegateStartFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.springframework.util.CollectionUtils;

/**
 * Command for retrieving start or form keys by process definition ids.
 */
@Slf4j
@EqualsAndHashCode
public class GetStartFormKeysCmd implements Command<Map<String, String>> {

  private final Set<String> processDefinitionIds;

  public GetStartFormKeysCmd(Set<String> processDefinitionIds) {
    this.processDefinitionIds = processDefinitionIds;
  }

  @Override
  public Map<String, String> execute(CommandContext commandContext) {
    if (CollectionUtils.isEmpty(processDefinitionIds)) {
      return Collections.emptyMap();
    }
    var processEngineConfiguration = Context.getProcessEngineConfiguration();
    var processEngine = processEngineConfiguration.getProcessEngine();

    var processDefinitionList = processEngine.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionIdIn(processDefinitionIds.toArray(new String[0]))
        .list();
    log.trace("Found process definitions {}", processDefinitionList);

    var deploymentCache = processEngineConfiguration.getDeploymentCache();

    return processDefinitionList.stream()
        .map(ProcessDefinitionEntity.class::cast)
        .filter(pd -> pd.hasStartFormKey() && isCached(pd, deploymentCache))
        .map(deploymentCache::resolveProcessDefinition)
        .collect(Collectors.toMap(CoreModelElement::getId, this::getStartFormKey));
  }

  /**
   * Logic of getting form key the same as in {@link org.camunda.bpm.engine.impl.cmd.GetFormKeyCmd},
   * see block for getting start form key.
   */
  public String getStartFormKey(ProcessDefinitionEntity processDefinition) {
    Expression formKeyExpression = null;
    var formHandler = processDefinition.getStartFormHandler();
    if (formHandler instanceof DelegateStartFormHandler) {
      var delegateFormHandler = (DelegateStartFormHandler) formHandler;
      formHandler = delegateFormHandler.getFormHandler();
    }
    if (formHandler instanceof DefaultStartFormHandler) {
      var startFormHandler = (DefaultStartFormHandler) formHandler;
      formKeyExpression = startFormHandler.getFormKey();
    }
    return Optional.ofNullable(formKeyExpression).map(Expression::getExpressionText)
        .orElseThrow(() -> new IllegalStateException("Couldn't get form key expression"));
  }

  private boolean isCached(ProcessDefinitionEntity processDefinitionEntity,
      DeploymentCache deploymentCache) {
    try {
      deploymentCache.resolveProcessDefinition(processDefinitionEntity);
      return true;
    } catch (NullValueException ex) {
      log.warn("ProcessDefinition with specified ProcessDefinitionId {} is not presented in cache",
          processDefinitionEntity.getId());
      return false;
    }
  }
}
