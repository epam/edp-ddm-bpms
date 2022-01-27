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

package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BusinessKeyListenerIT extends BaseIT {

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  @Deployment(resources = {"bpmn/businessKeyListenerBusinessProcesses.bpmn"})
  void businessKeyTestProcess(String processDefinitionKey, String businessKey) {
    var processInstance = runtimeService
        .startProcessInstanceByKey(processDefinitionKey);

    var historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).singleResult();

    assertThat(historicProcessInstance.getBusinessKey()).isEqualTo(businessKey);
  }

  static Stream<Arguments> testArgumentProvider() {
    return Stream.of(
        arguments("businessKeyTestProcess_noExtensionAttributes", null),
        arguments("businessKeyTestProcess_severalExtensionAttributes", null),
        arguments("businessKeyTestProcess_invalidExpression", null),
        arguments("businessKeyTestProcess_longExpressionResult", null),
        arguments("businessKeyTestProcess_validExpression", "businessKey")
    );
  }
}
