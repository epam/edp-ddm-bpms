package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;
import ua.gov.mdtu.ddm.general.errorhandling.exception.ValidationException;

/**
 * The class represents an implementation of {@link ExceptionMapper<ValidationException>} that is
 * used to map {@link ValidationException} to response
 */
@Provider
public class UserDataValidationExceptionMapper implements
    ExceptionMapper<ValidationException> {

  @Override
  public Response toResponse(ValidationException ex) {
    return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(ex)
        .type(MediaType.APPLICATION_JSON).build();
  }
}
