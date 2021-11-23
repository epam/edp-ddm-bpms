/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.rest.exception.mapper;

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExceptionMapper<TaskAlreadyInCompletionException>}
 * that is used to map {@link TaskAlreadyInCompletionException} to 409-CONFLICT response
 */
@Slf4j
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
    log.error("User task already in completion", exception);
    return Response.status(Status.CONFLICT).entity(systemErrorDto).build();
  }
}
