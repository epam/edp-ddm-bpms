/*
 * Copyright 2023 EPAM Systems.
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

import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

import java.util.List;

public class UserDataValidationUtils {
  public static final String VALIDATION_ERROR_MSG_PATTERN =
          "Business validation failure occurred during process execution: "
                  + "process definition key %s, process instance id %s, activity id %s";

  public static ValidationErrorDto createUserDataValidationErrorDto(
          List<ErrorDetailDto> validationErrorDtos, DelegateExecution execution) {
    return ValidationErrorDto.builder()
            .code("VALIDATION_ERROR")
            .message(buildValidationErrorMsg(execution))
            .details(new ErrorsListDto(validationErrorDtos))
            .build();
  }

  private static String buildValidationErrorMsg(DelegateExecution execution) {
    return String.format(VALIDATION_ERROR_MSG_PATTERN,
            ((ExecutionEntity) execution).getProcessDefinition().getKey(),
            execution.getProcessInstanceId(), execution.getCurrentActivityId());
  }
}
