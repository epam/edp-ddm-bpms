package ua.gov.mdtu.ddm.lowcode.bpms.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.camunda.bpm.engine.rest.exception.ExceptionHandlerHelper;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.slf4j.MDC;
import ua.gov.mdtu.ddm.general.errorhandling.BaseRestExceptionHandler;
import ua.gov.mdtu.ddm.general.errorhandling.dto.SystemErrorDto;

public class CamundaRestExceptionMapper implements ExceptionMapper<RestException> {

  @Override
  public Response toResponse(RestException throwable) {
    var errorDto = ExceptionHandlerHelper.getInstance().fromException(throwable);

    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(errorDto.getType())
        .message(errorDto.getMessage())
        .localizedMessage(throwable.getLocalizedMessage())
        .build();

    return Response.status(ExceptionHandlerHelper.getInstance().getStatus(throwable))
        .entity(systemErrorDto).type(MediaType.APPLICATION_JSON).build();
  }
}
