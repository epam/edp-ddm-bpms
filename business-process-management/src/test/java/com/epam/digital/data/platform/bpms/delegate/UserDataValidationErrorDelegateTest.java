package com.epam.digital.data.platform.bpms.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserDataValidationErrorDelegateTest {

  private UserDataValidationErrorDelegate delegate;
  @Mock
  private DelegateExecution delegateExecution;

  @Before
  public void init() {
    delegate = new UserDataValidationErrorDelegate(new ObjectMapper());
  }

  @Test
  public void testNoMessagesFromUser() {
    when(delegateExecution.hasVariable("validationErrors")).thenReturn(false);

    var exception = assertThrows(ValidationException.class,
        () -> delegate.execute(delegateExecution));

    assertThat(exception).isNotNull();
    assertThat(exception.getDetails()).isNotNull();
    assertThat(exception.getDetails().getErrors()).isEmpty();
  }
}
