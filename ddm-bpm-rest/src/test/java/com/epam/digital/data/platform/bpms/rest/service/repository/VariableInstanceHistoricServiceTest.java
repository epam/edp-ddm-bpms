package com.epam.digital.data.platform.bpms.rest.service.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.sysvar.Constants;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariableInstanceHistoricServiceTest {

  @InjectMocks
  private VariableInstanceHistoricService service;
  @Mock
  private HistoryService historyService;

  @Test
  void getSystemVariablesForProcessInstanceIds() {
    var processInstanceId = "processInstanceId";

    var queryMock = mock(HistoricVariableInstanceQuery.class);
    when(historyService.createHistoricVariableInstanceQuery()).thenReturn(queryMock);
    when(queryMock.processInstanceIdIn(processInstanceId)).thenReturn(queryMock);
    when(queryMock.variableNameLike(Constants.SYS_VAR_PREFIX_LIKE)).thenReturn(queryMock);

    var variable1 = buildHistoricVariableInstanceEntity(processInstanceId, "sys-var-1", null);
    var variable2 = buildHistoricVariableInstanceEntity(processInstanceId,
        ProcessCompletionResultVariable.SYS_VAR_PROCESS_COMPLETION_RESULT, "BP COMPLETED");
    var variable3 = buildHistoricVariableInstanceEntity(processInstanceId,
        ProcessExcerptIdVariable.SYS_VAR_PROCESS_EXCERPT_ID, "excerptId");

    when(queryMock.list()).thenReturn(List.of(variable1, variable2, variable3));

    var result = service.getSystemVariablesForProcessInstanceIds(processInstanceId);

    assertThat(result).hasSize(1)
        .extractingByKey(processInstanceId)
        .hasFieldOrPropertyWithValue("processCompletionResult", "BP COMPLETED")
        .hasFieldOrPropertyWithValue("excerptId", "excerptId");
  }

  private HistoricVariableInstanceEntity buildHistoricVariableInstanceEntity(
      String processInstanceId, String name, String value) {
    var variable = mock(HistoricVariableInstanceEntity.class);
    lenient().when(variable.getProcessInstanceId()).thenReturn(processInstanceId);
    lenient().when(variable.getName()).thenReturn(name);
    when(variable.getValue()).thenReturn(value);
    return variable;
  }
}
