package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ErrorDetailsDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.UserDataValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.api.dto.ValidationErrorDto;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

/**
 * Throws user data validation exception with details based on user input.
 */
@Component("userDataValidationErrorDelegate")
@RequiredArgsConstructor
public class UserDataValidationErrorDelegate implements JavaDelegate {

  private static final String VAR_VALIDATION_ERRORS = "validationErrors";

  private final ObjectMapper objectMapper;

  @Override
  @SuppressWarnings("unchecked")
  public void execute(DelegateExecution execution) {
    List<ValidationErrorDto> validationErrorDtos =
        execution.hasVariable(VAR_VALIDATION_ERRORS) ?
            ((List<String>) execution.getVariable(VAR_VALIDATION_ERRORS))
                .stream().map(this::readValidationErrorValue).collect(Collectors.toList())
            : Collections.emptyList();

    throw new UserDataValidationException(createUserDataValidationErrorDto(validationErrorDtos));
  }

  private ValidationErrorDto readValidationErrorValue(String value) {
    try {
      return objectMapper.readValue(value, ValidationErrorDto.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(String.format("couldn't serialize %s", value), ex);
    }
  }

  private UserDataValidationErrorDto createUserDataValidationErrorDto(
      List<ValidationErrorDto> validationErrorDtos) {
    return UserDataValidationErrorDto.builder()
        .code("VALIDATION_ERROR")
        .message("Validation error")
        .details(new ErrorDetailsDto(validationErrorDtos))
        .build();
  }
}
