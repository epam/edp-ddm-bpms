package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;

@Provider
public class CamundaSystemExceptionMapper implements
    ExceptionMapper<CamundaSystemException> {

  @Override
  public Response toResponse(CamundaSystemException ex) {
    return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        .entity(ex).type(MediaType.APPLICATION_JSON).build();
  }
}
