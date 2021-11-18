package com.epam.digital.data.platform.bpms.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.engine.cmd.GetStartFormKeysCmd;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartBatchFormServiceTest {

  @InjectMocks
  private BatchFormServiceImpl batchFormService;
  @Mock
  private CommandExecutor commandExecutor;

  @Test
  void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
    var existedProcessDefinition = "existed";
    var nonExistedProcessDefinition = "notExisted";

    when(commandExecutor.execute(new GetStartFormKeysCmd(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition)))).thenReturn(
        Map.of(existedProcessDefinition, "form-key"));

    var formKeys = batchFormService.getStartFormKeys(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition));

    assertThat(formKeys).containsEntry(existedProcessDefinition, "form-key");
  }
}
