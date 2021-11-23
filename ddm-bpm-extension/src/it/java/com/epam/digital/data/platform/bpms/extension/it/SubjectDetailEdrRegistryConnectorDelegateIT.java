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

package com.epam.digital.data.platform.bpms.extension.it;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

public class SubjectDetailEdrRegistryConnectorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/connector/testSubjectDetailEdrRegistryConnectorDelegate.bpmn"})
  public void shouldSearchSubjects() throws Exception {
    stubSubjectDetail("/xml/subjectDetailResponse.xml");

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_subject_detail_key");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
