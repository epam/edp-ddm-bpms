package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * The class represents an implementation of {@link ExceptionMapper<ValidationException>} that is
 * used to map {@link ValidationException} to response
 */
@Slf4j
@Provider
public class UserDataValidationExceptionMapper implements
    ExceptionMapper<ValidationException> {

  @Override
  public Response toResponse(ValidationException ex) {
    log.warn("User data validation error", ex);
    return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(ex)
        .type(MediaType.APPLICATION_JSON).build();
  }
}