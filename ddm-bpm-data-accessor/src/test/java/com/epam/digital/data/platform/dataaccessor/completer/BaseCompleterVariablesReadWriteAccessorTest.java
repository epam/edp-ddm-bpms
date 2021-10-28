package com.epam.digital.data.platform.dataaccessor.completer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.VariableAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseCompleterVariablesReadWriteAccessorTest {

  @InjectMocks
  private BaseCompleterVariablesReadWriteAccessor baseCompleterVariablesReadWriteAccessor;
  @Mock
  private VariableAccessor variableAccessor;

  @Test
  void setTaskCompleter() {
    var taskDefinitionKey = "task";
    var completerName = "completer";

    baseCompleterVariablesReadWriteAccessor.setTaskCompleter(taskDefinitionKey, completerName);

    verify(variableAccessor).setVariable("task_completer", completerName);
  }

  @Test
  void setTaskCompleterToken() {
    var taskDefinitionKey = "task";
    var completerToken = "completerToken";

    baseCompleterVariablesReadWriteAccessor.setTaskCompleterToken(taskDefinitionKey,
        completerToken);

    verify(variableAccessor).setVariableTransient("task_completer_access_token", completerToken);
  }

  @Test
  void getTaskCompleter() {
    var taskDefinitionKey = "task";
    var completerName = "completer";
    when(variableAccessor.getVariable("task_completer")).thenReturn(completerName);

    var result = baseCompleterVariablesReadWriteAccessor.getTaskCompleter(taskDefinitionKey);

    assertThat(result).isNotEmpty().get().isSameAs(completerName);
  }

  @Test
  void getTaskCompleterToken() {
    var taskDefinitionKey = "task";
    var completerToken = "token";
    when(variableAccessor.getVariable("task_completer_access_token")).thenReturn(completerToken);

    var result = baseCompleterVariablesReadWriteAccessor.getTaskCompleterToken(taskDefinitionKey);

    assertThat(result).isNotEmpty().get().isSameAs(completerToken);
  }
}
