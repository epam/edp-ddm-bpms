package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

@Provider
public class UserDataValidationExceptionMapper implements
    ExceptionMapper<UserDataValidationException> {

  @Override
  public Response toResponse(UserDataValidationException ex) {
    return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(ex.getErrorDto())
        .type(MediaType.APPLICATION_JSON).build();
  }
}
