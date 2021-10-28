package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UserDataValidationErrorDelegateTest {

  @InjectMocks
  private UserDataValidationErrorDelegate delegate;
  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();
  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private NamedVariableAccessor<List<String>> validationErrorsVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<List<String>> validationErrorsVariableReadAccessor;

  @Before
  public void init() {
    doReturn(validationErrorsVariableReadAccessor).when(validationErrorsVariableAccessor)
        .from(delegateExecution);

    ReflectionTestUtils.setField(delegate, "validationErrorsVariable",
        validationErrorsVariableAccessor);
  }

  @Test
  public void testNoMessagesFromUser() {
    when(validationErrorsVariableReadAccessor.getOptional()).thenReturn(Optional.empty());

    var exception = assertThrows(ValidationException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(exception).isNotNull();
    assertThat(exception.getDetails()).isNotNull();
    assertThat(exception.getDetails().getErrors()).isEmpty();
  }
}
