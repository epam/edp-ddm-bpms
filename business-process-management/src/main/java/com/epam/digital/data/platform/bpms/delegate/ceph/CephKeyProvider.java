package com.epam.digital.data.platform.bpms.delegate.ceph;

import org.springframework.stereotype.Component;

/**
 * The class represents a provider that is used to generate the key to get the ceph document
 */
@Component
public class CephKeyProvider {

  private static final String TASK_FORM_DATA_CEPH_KEY_FORMAT = "process/%s/task/%s";

  /**
   * Method for generating the ceph key, uses task definition key and process instance identifier to
   * construct the key
   *
   * @param taskDefinitionKey task definition key
   * @param processInstanceId process instance identifier
   * @return generated ceph key
   */
  public String generateKey(String taskDefinitionKey, String processInstanceId) {
    return String.format(TASK_FORM_DATA_CEPH_KEY_FORMAT, processInstanceId, taskDefinitionKey);
  }
}
