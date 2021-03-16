package ua.gov.mdtu.ddm.lowcode.bpms.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskPropertyServiceImpl implements TaskPropertyService {

  private final ProcessEngine processEngine;

  @Override
  public Map<String, String> getTaskProperty(String taskId) {
    Map<String, String> taskProperties = new HashMap<>();
    List<CamundaProperty> properties = getCamundaProperties(taskId);
    properties.forEach(pr -> taskProperties.put(pr.getCamundaName(), pr.getCamundaValue()));
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
    List<CamundaProperty> camundaProperties = new ArrayList<>();
    Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    if (Objects.nonNull(task)) {
      Collection<UserTask> userTasks = processEngine
          .getRepositoryService()
          .getBpmnModelInstance(task.getProcessDefinitionId())
          .getModelElementsByType(UserTask.class);
      camundaProperties = userTasks.stream()
          .filter(userTask -> userTask.getId().equals(task.getTaskDefinitionKey()))
          .map(UserTask::getExtensionElements)
          .filter(Objects::nonNull)
          .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
          .flatMap(e -> e.getCamundaProperties().stream())
          .collect(Collectors.toList());
    }
    return camundaProperties;
  }
}
