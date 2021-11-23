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

package com.epam.digital.data.platform.bpms.extension.it;

import com.epam.digital.data.platform.bpms.extension.it.builder.StubData;
import com.epam.digital.data.platform.excerpt.model.ExcerptEntityId;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

public class ExcerptConnectorGenerateDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = "/bpmn/connector/testGenerateExcerpt.bpmn")
  public void shouldGenerateExcerpt() throws Exception {
    var requestDto = new ExcerptEventDto();
    requestDto.setExcerptType("subject-laboratories-accreditation-excerpt");
    requestDto.setExcerptInputData(Map.of("subjectId", "1234"));
    requestDto.setRequiresSystemSignature(true);

    stubExcerptServiceRequest(StubData.builder()
        .httpMethod(HttpMethod.POST)
        .resource("excerpts")
        .requestBody(objectMapper.writeValueAsString(requestDto))
        .response(objectMapper.writeValueAsString(
            new ExcerptEntityId(UUID.fromString("d564f2ab-eec6-11eb-9efa-0a580a820439"))))
        .build());

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_generate_excerpt");

    var processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
