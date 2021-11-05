package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.camunda.bpm.engine.rest.exception.ExceptionHandlerHelper;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.slf4j.MDC;

/**
 * The class represents an implementation of {@link ExceptionMapper<RestException>} that is used to
 * map camunda {@link RestException} to response
 */
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
