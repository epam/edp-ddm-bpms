package com.epam.digital.data.platform.bpms.delegate;

import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
@Component("userDataValidationErrorDelegate")
@RequiredArgsConstructor
public class UserDataValidationErrorDelegate implements JavaDelegate {

  private static final String VAR_VALIDATION_ERRORS = "validationErrors";

  private final ObjectMapper objectMapper;

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) {
    List<ErrorDetailDto> validationErrorDtos =
        execution.hasVariable(VAR_VALIDATION_ERRORS) ?
            ((List<String>) execution.getVariable(VAR_VALIDATION_ERRORS))
                .stream().map(this::readValidationErrorValue).collect(Collectors.toList())
            : Collections.emptyList();

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
}
