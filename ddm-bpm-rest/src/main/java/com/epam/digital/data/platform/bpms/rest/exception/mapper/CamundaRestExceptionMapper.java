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

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.rest.exception.ExceptionHandlerHelper;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.slf4j.MDC;

/**
 * The class represents an implementation of {@link ExceptionMapper<RestException>} that is used to
 * map camunda {@link RestException} to response
 */
@Slf4j
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
    log.error("Camunda rest communication error", throwable);
    return Response.status(ExceptionHandlerHelper.getInstance().getStatus(throwable))
        .entity(systemErrorDto).type(MediaType.APPLICATION_JSON).build();
  }
}
