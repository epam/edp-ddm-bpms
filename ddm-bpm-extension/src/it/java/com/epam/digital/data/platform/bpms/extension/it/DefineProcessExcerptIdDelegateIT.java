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
