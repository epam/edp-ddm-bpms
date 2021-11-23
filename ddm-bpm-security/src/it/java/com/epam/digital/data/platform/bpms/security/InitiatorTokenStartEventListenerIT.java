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

package com.epam.digital.data.platform.bpms.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class InitiatorTokenStartEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/initiator_access_token.bpmn")
  public void testInitiatorAccessToken() throws JsonProcessingException {
    var result = postForObject("api/process-definition/key/initiator_access_token/start",
        "{}", Map.class);

    assertThat((Boolean) result.get("ended")).isTrue();
  }
}
