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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.integration.idm.exception.KeycloakException;
import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

class KeycloakExceptionMapperTest {

  @Test
  void testKeycloakNotFoundExceptionMapper() {
    var traceId = "traceId";
    var code = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR);
    var message = "message";
    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, traceId);

    var response = new KeycloakExceptionMapper()
        .toResponse(new KeycloakException(message));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(response.getEntity()).isEqualTo(SystemErrorDto.builder()
        .traceId(traceId)
        .code(code)
        .message(message)
        .build());
  }
}
