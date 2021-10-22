package com.epam.digital.data.platform.bpms.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.engine.cmd.GetStartFormKeysCmd;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartBatchFormServiceTest {

  private BatchFormServiceImpl batchFormService;
  @Mock
  private CommandExecutor commandExecutor;

  @Before
  public void init() {
    batchFormService = new BatchFormServiceImpl();
    batchFormService.setCommandExecutor(commandExecutor);
  }

  @Test
  public void shouldReturnEmptyMapAsTaskHasEmptyProperty() {
    var existedProcessDefinition = "existed";
    var nonExistedProcessDefinition = "notExisted";

    when(commandExecutor.execute(eq(new GetStartFormKeysCmd(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition))))).thenReturn(
        Map.of(existedProcessDefinition, "form-key"));

    var formKeys = batchFormService.getStartFormKeys(
        Set.of(existedProcessDefinition, nonExistedProcessDefinition));

    assertThat(formKeys).containsEntry(existedProcessDefinition, "form-key");
  }
}
