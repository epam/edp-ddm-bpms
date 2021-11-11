package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.extension.exception.KeycloakException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

class KeycloakExceptionMapperTest {

  @Test
  void testKeycloakNotFoundExceptionMapper() {
    var traceId = "traceId";
    var code = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR);
    var message = "message";
    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, traceId);

    var response = new KeycloakExceptionMapper()
        .toResponse(new KeycloakException(message));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(response.getEntity()).isEqualTo(SystemErrorDto.builder()
        .traceId(traceId)
        .code(code)
        .message(message)
        .build());
  }
}
