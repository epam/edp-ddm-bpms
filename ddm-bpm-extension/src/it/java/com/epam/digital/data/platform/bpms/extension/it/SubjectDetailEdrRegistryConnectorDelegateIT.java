package com.epam.digital.data.platform.bpms.extension.it;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

public class SubjectDetailEdrRegistryConnectorDelegateIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/connector/testSubjectDetailEdrRegistryConnectorDelegate.bpmn"})
  public void shouldSearchSubjects() throws Exception {
    stubSubjectDetail("/xml/subjectDetailResponse.xml");

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_subject_detail_key");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}
