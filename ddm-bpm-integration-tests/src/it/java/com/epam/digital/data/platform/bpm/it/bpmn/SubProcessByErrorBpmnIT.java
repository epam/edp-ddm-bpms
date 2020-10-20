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

package com.epam.digital.data.platform.bpm.it.bpmn;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class SubProcessByErrorBpmnIT extends BaseBpmnIT {

  @Test
  @Deployment(resources = "bpmn/sub-process-by-error.bpmn")
  public void testCatchError() throws JsonProcessingException {
    var subProcessByCatchErrorProcessInstanceId = startProcessInstance(
        "start_sub_process_by_catch_error", testUserToken);

    var subProcessByCatchErrorProcessInstance = BpmnAwareTests.historyService()
        .createHistoricProcessInstanceQuery()
        .processInstanceId(subProcessByCatchErrorProcessInstanceId)
        .singleResult();

    var variables = BpmnAwareTests.historyService()
        .createHistoricVariableInstanceQuery()
        .processInstanceId(subProcessByCatchErrorProcessInstanceId)
        .variableName("variable")
        .list();

    Assertions.assertThat(subProcessByCatchErrorProcessInstance)
        .isNotNull()
        .extracting(HistoricProcessInstance::getState)
        .isEqualTo("COMPLETED");

    Assertions.assertThat(variables)
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("name", "variable")
        .hasFieldOrPropertyWithValue("value", "value");
  }

  @Test
  @Deployment(resources = "bpmn/sub-process-by-error.bpmn")
  public void testThrowError() throws JsonProcessingException {
    var subProcessByThrowErrorProcessInstanceId = startProcessInstance(
        "start_sub_process_by_throw_error", testUserToken);

    var subProcessByThrowErrorProcessInstance = BpmnAwareTests.historyService()
        .createHistoricProcessInstanceQuery()
        .processInstanceId(subProcessByThrowErrorProcessInstanceId)
        .singleResult();

    var variables = BpmnAwareTests.historyService()
        .createHistoricVariableInstanceQuery()
        .processInstanceId(subProcessByThrowErrorProcessInstanceId)
        .variableName("variable")
        .list();

    Assertions.assertThat(subProcessByThrowErrorProcessInstance)
        .isNotNull()
        .extracting(HistoricProcessInstance::getState)
        .isEqualTo("COMPLETED");

    Assertions.assertThat(variables)
        .hasSize(1)
        .element(0)
        .hasFieldOrPropertyWithValue("name", "variable")
        .hasFieldOrPropertyWithValue("value", "value");
  }
}
