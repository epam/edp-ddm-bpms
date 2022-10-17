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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

class ConstraintViolationExceptionMapperTest {

  @Test
  void testConstraintViolationExceptionMapper() {
    var traceId = "traceId";
    var code = String.valueOf(HttpStatus.CONFLICT);
    var message = "message";
    var localizedMessage = "Порушення одного з обмежень на рівні БД";
    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, traceId);

    var response = new ConstraintViolationExceptionMapper()
        .toResponse(new ConstraintViolationException(traceId, code, message, localizedMessage));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(response.getEntity()).isEqualTo(SystemErrorDto.builder()
        .traceId(traceId)
        .code(code)
        .message(message)
        .localizedMessage(localizedMessage)
        .build());
  }
}
