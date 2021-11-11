package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import com.epam.digital.data.platform.bpms.extension.exception.KeycloakException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

/**
 * The class represents an implementation of {@link ExceptionMapper<KeycloakException>} that is
 * used to map exception {@link KeycloakException} to response
 */
@Slf4j
public class KeycloakExceptionMapper implements ExceptionMapper<KeycloakException> {

  @Override
  public Response toResponse(KeycloakException ex) {
    log.error(ex.getMessage(), ex.getCause());
    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
        .message(ex.getMessage())
        .build();
    log.error("Keycloak communication error", ex);
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(systemErrorDto)
        .type(MediaType.APPLICATION_JSON).build();
  }
}
