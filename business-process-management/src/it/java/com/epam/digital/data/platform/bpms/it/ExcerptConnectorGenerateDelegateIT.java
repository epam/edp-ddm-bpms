package com.epam.digital.data.platform.bpms.it;

import com.epam.digital.data.platform.bpms.it.builder.StubData;
import com.epam.digital.data.platform.bpms.it.camunda.bpmn.BaseBpmnIT;
import com.epam.digital.data.platform.excerpt.model.ExcerptEntityId;
import com.epam.digital.data.platform.excerpt.model.ExcerptEventDto;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

public class ExcerptConnectorGenerateDelegateIT extends BaseBpmnIT {

  @Test
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
