package com.epam.digital.data.platform.bpms.cmd;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler;
import org.camunda.bpm.engine.impl.form.handler.DelegateStartFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.util.CollectionUtils;

/**
 * Command for retrieving start or form keys by process definition ids.
 */
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

    var deploymentCache = processEngineConfiguration.getDeploymentCache();

    return processDefinitionList.stream()
        .filter(ProcessDefinition::hasStartFormKey)
        .map(pd -> deploymentCache.resolveProcessDefinition((ProcessDefinitionEntity) pd))
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

}
