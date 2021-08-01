package com.epam.digital.data.platform.bpms.delegate.ceph;

import org.springframework.stereotype.Component;

/**
 * The class represents a provider that is used to generate the key to get the ceph document
 */
@Component
public class CephKeyProvider {

  private static final String TASK_FORM_DATA_STRING_FORMAT = "secure-sys-var-ref-task-form-data-%s";
  private static final String TASK_FORM_DATA_VALUE_FORMAT = "lowcode-%s-%s";

  /**
   * Method for generating the ceph key, uses task definition key and process instance identifier to
   * construct the key
   *
   * @param taskDefinitionKey task definition key
   * @param processInstanceId process instance identifier
   * @return generated ceph key
   */
  public String generateKey(String taskDefinitionKey, String processInstanceId) {
    var taskFormDataVariableName = String.format(TASK_FORM_DATA_STRING_FORMAT, taskDefinitionKey);
    return String.format(TASK_FORM_DATA_VALUE_FORMAT, processInstanceId, taskFormDataVariableName);
  }
}
