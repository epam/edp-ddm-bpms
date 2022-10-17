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

import com.epam.digital.data.platform.starter.errorhandling.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

class CamundaForbiddenOperationExceptionMapperTest {

  @Test
  void testUserDataValidationExceptionMapper() {
    var ex = new ForbiddenOperationException(null);

    Response response = new CamundaForbiddenOperationExceptionMapper().toResponse(ex);

    assertThat(response.getMediaType()).hasToString(MediaType.APPLICATION_JSON);
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getEntity()).isSameAs(ex);
  }
}