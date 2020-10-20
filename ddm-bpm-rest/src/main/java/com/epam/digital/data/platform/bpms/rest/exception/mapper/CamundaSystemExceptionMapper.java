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

import com.epam.digital.data.platform.starter.errorhandling.exception.SystemException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * The class represents an implementation of {@link ExceptionMapper<SystemException>} that is used
 * to map {@link SystemException} to response
 */
@Slf4j
@Provider
public class CamundaSystemExceptionMapper implements ExceptionMapper<SystemException> {

  @Override
  public Response toResponse(SystemException ex) {
    log.error("Camunda system error", ex);
    return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        .entity(ex).type(MediaType.APPLICATION_JSON).build();
  }
}
