package com.epam.digital.data.platform.bpms.exception.mapper;

import com.epam.digital.data.platform.bpms.exception.KeycloakNotFoundException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

/**
 * The class represents an implementation of {@link ExceptionMapper< KeycloakNotFoundException >}
 * that is used to map exception {@link KeycloakNotFoundException} to response
 */
public class KeycloakNotFoundExceptionMapper implements
    ExceptionMapper<KeycloakNotFoundException> {

  @Override
  public Response toResponse(KeycloakNotFoundException ex) {
    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
        .message(ex.getMessage())
        .build();
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(systemErrorDto)
        .type(MediaType.APPLICATION_JSON).build();
  }
}
