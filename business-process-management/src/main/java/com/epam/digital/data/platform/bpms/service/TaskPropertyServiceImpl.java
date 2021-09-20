package com.epam.digital.data.platform.bpms.service;

import com.epam.digital.data.platform.bpms.security.CamundaImpersonation;
import com.epam.digital.data.platform.bpms.security.CamundaImpersonationFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskPropertyServiceImpl implements TaskPropertyService {

  private final ProcessEngine processEngine;
  @Qualifier("camundaAdminImpersonationFactory")
  private final CamundaImpersonationFactory camundaImpersonationFactory;

  @Override
  public Map<String, String> getTaskProperty(String taskId) {
    log.info("Getting task {} properties", taskId);
    Map<String, String> taskProperties = new HashMap<>();
    var properties = getCamundaProperties(taskId);
    properties.forEach(pr -> taskProperties.put(pr.getCamundaName(), pr.getCamundaValue()));
    log.info("Found {} properties for task {}", taskProperties.size(), taskId);
    return taskProperties;
  }

  /**
   * Returns a list containing the CamundaProperty objects of the task.
   * <p>
   * This method returns an empty list if the task has no properties or if there is no task with
   * this taskId.
   *
   * @param taskId - task identifier
   * @return a list containing the CamundaProperty objects of the task
   */
  private List<CamundaProperty> getCamundaProperties(String taskId) {
    var optionalImpersonation = camundaImpersonationFactory.getCamundaImpersonation();
    if (optionalImpersonation.isEmpty()) {
      throw new IllegalStateException(
          String.format("Error occurred during getting camunda extension properties for task %s. "
              + "There is no user that authenticated in camunda", taskId));
    }
    var adminImpersonation = optionalImpersonation.get();

    var task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if (Objects.isNull(task)) {
      return Collections.emptyList();
    }

    return getUserTasks(adminImpersonation, task).stream()
        .filter(userTask -> userTask.getId().equals(task.getTaskDefinitionKey()))
        .map(UserTask::getExtensionElements)
        .filter(Objects::nonNull)
        .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
        .flatMap(e -> e.getCamundaProperties().stream())
        .collect(Collectors.toList());
  }

  private Collection<UserTask> getUserTasks(CamundaImpersonation adminImpersonation, Task task) {
    adminImpersonation.impersonate();
    try {
      return processEngine.getRepositoryService()
          .getBpmnModelInstance(task.getProcessDefinitionId())
          .getModelElementsByType(UserTask.class);
    } finally {
      adminImpersonation.revertToSelf();
    }
  }
}
