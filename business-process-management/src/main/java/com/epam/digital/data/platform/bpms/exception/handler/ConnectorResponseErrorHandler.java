package com.epam.digital.data.platform.bpms.exception.handler;

import com.epam.digital.data.platform.bpms.delegate.dto.enums.DataFactoryError;
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
      log.info("Request failed with validationException with code {}, message - {} and "
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
