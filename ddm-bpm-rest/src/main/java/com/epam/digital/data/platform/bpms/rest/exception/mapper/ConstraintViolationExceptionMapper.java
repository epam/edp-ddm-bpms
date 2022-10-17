/*
 * Copyright 2022 EPAM Systems.
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
import com.epam.digital.data.platform.starter.errorhandling.exception.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(ConstraintViolationException ex) {
    log.error(ex.getMessage(), ex.getCause());
    var systemErrorDto = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(ex.getCode())
        .message(ex.getMessage())
        .localizedMessage(ex.getLocalizedMessage())
        .build();
    log.error("Constraint violation error", ex);
    return Response.status(Status.CONFLICT).entity(systemErrorDto)
        .type(MediaType.APPLICATION_JSON).build();
  }
}
