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

package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

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
