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

package com.epam.digital.data.platform.bpms.extension.exception.handler;

import com.epam.digital.data.platform.bpms.extension.delegate.dto.enums.DataFactoryError;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorResponseErrorHandler extends DefaultResponseErrorHandler {

  private final ObjectMapper objectMapper;
  private final MessageResolver messageResolver;

  @Override
  public void handleError(@NonNull URI url, @NonNull HttpMethod method, ClientHttpResponse response)
      throws IOException {
    if (isValidationException(method, response.getStatusCode())) {
      var exception = validationException(response);
      log.warn("Request failed with validationException with code {}, message - {} and "
          + "details - {}", exception.getCode(), exception.getMessage(), exception.getDetails());
      throw exception;
    }
    var exception = systemException(response.getBody());
    log.error("Request failed with systemException with code {}, message - {} and "
            + "localizedMessage - {}", exception.getCode(), exception.getMessage(),
        exception.getLocalizedMessage());
    throw exception;
  }

  private boolean isValidationException(HttpMethod method, HttpStatus status) {
    if (HttpStatus.UNPROCESSABLE_ENTITY.equals(status)) {
      return true;
    }
    if (HttpMethod.GET.equals(method)) {
      return HttpStatus.NOT_FOUND.equals(status);
    }
    return false;
  }

  @SneakyThrows
  private SystemException systemException(InputStream responseBody) {
    var systemErrorDto = objectMapper.readValue(responseBody, SystemErrorDto.class);

    var dataFactoryError = DataFactoryError.fromNameOrDefaultRuntimeError(systemErrorDto.getCode());
    var localizedMessage = messageResolver.getMessage(dataFactoryError.getTitleKey());

    systemErrorDto.setLocalizedMessage(localizedMessage);
    return new SystemException(systemErrorDto);
  }

  @SneakyThrows
  private ValidationException validationException(ClientHttpResponse response) {
    var validationErrorDto = objectMapper.readValue(response.getBody(), ValidationErrorDto.class);

    if (Objects.nonNull(validationErrorDto.getDetails())) {
      var localizedMessage = messageResolver
          .getMessage(DataFactoryError.VALIDATION_ERROR.getTitleKey());
      validationErrorDto.getDetails().getErrors()
          .forEach(errorDetailDto -> errorDetailDto.setMessage(localizedMessage));
    } else if (HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
      var localizedMessage = messageResolver.getMessage(DataFactoryError.NOT_FOUND.getTitleKey());
      validationErrorDto.setDetails(new ErrorsListDto(Collections.singletonList(
          new ErrorDetailDto(localizedMessage, null, null))));
    }

    return new ValidationException(validationErrorDto);
  }
}
