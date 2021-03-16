package ua.gov.mdtu.ddm.lowcode.bpms.service;

import java.util.Map;

public interface TaskPropertyService {

  /**
   * Returns a map containing the properties of the task. Key of the map is a property name, Value -
   * property value.
   * <p>
   * This method returns an empty map if the task has no properties or task with this taskId is
   * absent.
   *
   * @param taskId - task identifier
   * @return a map containing the properties of the task
   */
  Map<String, String> getTaskProperty(String taskId);
}
