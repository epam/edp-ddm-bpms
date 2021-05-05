package com.epam.digital.data.platform.bpms.exception.mapper;

import com.epam.digital.data.platform.bpms.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExceptionMapper<TaskAlreadyInCompletionException>}
 * that is used to map {@link TaskAlreadyInCompletionException} to 409-CONFLICT response
 */
@Provider
@Component
@RequiredArgsConstructor
public class TaskAlreadyInCompletionExceptionMapper implements
    ExceptionMapper<TaskAlreadyInCompletionException> {

  private static final String TASK_ALREADY_IN_COMPLETION_EXCEPTION_MESSAGE_KEY = "task.already.in.completion";

  private final MessageResolver messageResolver;

  @Override
  public Response toResponse(TaskAlreadyInCompletionException exception) {
    var localizedMessage = messageResolver
        .getMessage(TASK_ALREADY_IN_COMPLETION_EXCEPTION_MESSAGE_KEY);
    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(String.valueOf(HttpStatus.CONFLICT))
        .message(exception.getMessage())
        .localizedMessage(localizedMessage)
        .build();

    return Response.status(Status.CONFLICT).entity(systemErrorDto).build();
  }
}
