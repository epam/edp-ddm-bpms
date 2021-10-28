package com.epam.digital.data.platform.bpms.extension.delegate;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to throw a user data
 * validation exception with details based on user input.
 */
@Component(UserDataValidationErrorDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class UserDataValidationErrorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "userDataValidationErrorDelegate";

  private final ObjectMapper objectMapper;

  @SystemVariable(name = "validationErrors")
  private NamedVariableAccessor<List<String>> validationErrorsVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var validationErrorDtos = validationErrorsVariable.from(execution)
        .getOptional().stream().flatMap(Collection::stream)
        .map(this::readValidationErrorValue)
        .collect(Collectors.toList());

    throw new ValidationException(createUserDataValidationErrorDto(validationErrorDtos));
  }

  private ErrorDetailDto readValidationErrorValue(String value) {
    try {
      return objectMapper.readValue(value, ErrorDetailDto.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(String.format("couldn't serialize %s", value), ex);
    }
  }

  private ValidationErrorDto createUserDataValidationErrorDto(
      List<ErrorDetailDto> validationErrorDtos) {
    return ValidationErrorDto.builder()
        .code("VALIDATION_ERROR")
        .message("Validation error")
        .details(new ErrorsListDto(validationErrorDtos))
        .build();
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
