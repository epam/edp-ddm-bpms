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

import com.epam.digital.data.platform.starter.errorhandling.exception.ForbiddenOperationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * The class represents an implementation of {@link ExceptionMapper<ForbiddenOperationException>} that is used
 * to map {@link ForbiddenOperationException} to response
 */
@Slf4j
@Provider
public class CamundaForbiddenOperationExceptionMapper implements ExceptionMapper<ForbiddenOperationException> {

  @Override
  public Response toResponse(ForbiddenOperationException ex) {
    log.error("Camunda forbidden operation error", ex);
    return Response.status(HttpStatus.SC_FORBIDDEN)
        .entity(ex).type(MediaType.APPLICATION_JSON).build();
  }
}
