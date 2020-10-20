/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to throw a user data
 * validation exception with details based on user input.
 */
@Component(UserDataValidationErrorDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class UserDataValidationErrorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "userDataValidationErrorDelegate";
  public static final String VALIDATION_ERROR_MSG_PATTERN =
      "Business validation failure occurred during process execution: "
          + "process definition key %s, process instance id %s, activity id %s";

  private final ObjectMapper objectMapper;

  @SystemVariable(name = "validationErrors")
  private NamedVariableAccessor<List<String>> validationErrorsVariable;

  @Override
  public void executeInternal(DelegateExecution execution) {
    var validationErrorDtos = validationErrorsVariable.from(execution)
        .getOptional().stream().flatMap(Collection::stream)
        .map(this::readValidationErrorValue)
        .collect(Collectors.toList());

    throw new ValidationException(createUserDataValidationErrorDto(validationErrorDtos, execution));
  }

  private ErrorDetailDto readValidationErrorValue(String value) {
    try {
      return objectMapper.readValue(value, ErrorDetailDto.class);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(String.format("couldn't serialize %s", value), ex);
    }
  }

  private ValidationErrorDto createUserDataValidationErrorDto(
      List<ErrorDetailDto> validationErrorDtos, DelegateExecution execution) {
    return ValidationErrorDto.builder()
        .code("VALIDATION_ERROR")
        .message(buildValidationErrorMsg(execution))
        .details(new ErrorsListDto(validationErrorDtos))
        .build();
  }

  private String buildValidationErrorMsg(DelegateExecution execution) {
    return String.format(VALIDATION_ERROR_MSG_PATTERN,
        ((ExecutionEntity) execution).getProcessDefinition().getKey(),
        execution.getProcessInstanceId(), execution.getCurrentActivityId());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
