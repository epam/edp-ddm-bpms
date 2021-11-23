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

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.util.Objects;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class DefineProcessExcerptIdDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/connector/testDefineProcessExcerptIdDelegate.bpmn")
  public void testDefineProcessExcerptId() {
    var processInstance = runtimeService
        .startProcessInstanceByKey("testDefineProcessExcerptId");

    BpmnAwareTests.assertThat(processInstance).isEnded();

    var resultVariables = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getId()).list().stream()
        .filter(historicVariableInstance -> Objects.nonNull(historicVariableInstance.getValue()))
        .collect(toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
            (o1, o2) -> o1));

    assertThat(resultVariables).containsKey(ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID);
  }
}
