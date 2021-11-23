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

package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.CephKeyProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CephKeyProviderTest {

  private CephKeyProvider cephKeyProvider;

  @Before
  public void init() {
    cephKeyProvider = new CephKeyProvider();
  }

  @Test
  public void testGeneratingCephKey() {
    var expectedKey = "process/testProcessInstanceId/task/testTaskDefinitionKey";
    var taskDefinitionKey = "testTaskDefinitionKey";
    var processInstanceId = "testProcessInstanceId";

    var actualKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    assertThat(actualKey).isEqualTo(expectedKey);
  }
}
