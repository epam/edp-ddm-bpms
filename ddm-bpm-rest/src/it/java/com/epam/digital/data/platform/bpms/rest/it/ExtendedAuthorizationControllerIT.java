/*
 * Copyright 2023 EPAM Systems.
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


package com.epam.digital.data.platform.bpms.rest.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.bpms.api.dto.DdmCountResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.junit.jupiter.api.Test;

class ExtendedAuthorizationControllerIT extends BaseIT {

  @Test
  void shouldCreateProcessDefinitionAuthorizations() throws Exception {
    var expectedGroupId = "officer-pd-test";

    var result = postForObject("api/extended/authorizations/process-definition/create",
        "[{\"groupId\":\"officer-pd-test\", \"processDefinitionId\":\"123\"}]",
        DdmCountResultDto.class);

    var authorization = authorizationService.createAuthorizationQuery()
        .groupIdIn(expectedGroupId).list().get(0);
    var permissions = authorization.getPermissions(
        new Permission[]{Permissions.READ, Permissions.CREATE_INSTANCE});

    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
    assertThat(authorization.getGroupId()).isEqualTo(expectedGroupId);
    assertThat(permissions).hasSize(2);
  }

  @Test
  void shouldCreateProcessInstanceAuthorizations() throws Exception {
    var expectedGroupId = "officer-pi-test";

    var result = postForObject("api/extended/authorizations/process-instance/create",
        "[\"officer-pi-test\"]", DdmCountResultDto.class);

    var authorization = authorizationService.createAuthorizationQuery()
        .groupIdIn(expectedGroupId).list().get(0);
    var permissions = authorization.getPermissions(new Permission[]{Permissions.CREATE});

    assertThat(result).isNotNull();
    assertThat(result.getCount()).isEqualTo(1);
    assertThat(authorization.getGroupId()).isEqualTo(expectedGroupId);
    assertThat(authorization.getResourceId()).isEqualTo("*");
    assertThat(permissions).hasSize(1);
  }

  @Test
  void shouldDeleteAuthorizations() throws Exception {
    // 6 authorizations will be created by BaseIT#setAuthorization() method

    var result = deleteForAuthorization("api/extended/authorizations/delete");

    assertThat(result).isNotNull();
    assertThat(result.getCount()).isGreaterThanOrEqualTo(6);
  }

  private DdmCountResultDto deleteForAuthorization(String url) throws JsonProcessingException {
    String jsonResponse = jerseyClient.target(String.format("http://localhost:%d/%s", port, url))
        .request()
        .header(TOKEN_HEADER, validAccessToken)
        .delete()
        .readEntity(String.class);
    return objectMapper.readValue(jsonResponse, DdmCountResultDto.class);
  }
}