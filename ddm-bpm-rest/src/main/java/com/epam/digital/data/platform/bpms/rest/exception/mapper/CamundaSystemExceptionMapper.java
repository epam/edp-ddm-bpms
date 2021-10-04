package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * The class represents an implementation of {@link ExceptionMapper<SystemException>} that is used
 * to map {@link SystemException} to response
 */
@Slf4j
@Provider
public class CamundaSystemExceptionMapper implements ExceptionMapper<SystemException> {

  @Override
  public Response toResponse(SystemException ex) {
    log.error("Camunda system error", ex);
    return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        .entity(ex).type(MediaType.APPLICATION_JSON).build();
  }
}