package com.epam.digital.data.platform.bpms.extension.it;

import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

public class GetCertificateByNameDracsRegistryDelegateIT extends BaseIT {

  @Test
  @SneakyThrows
  public void testGetCertificateByName() {
    stubGetCertByNumRoleNames("/xml/GetCertByNumRoleNamesResponse.xml");

    var processInstance = runtimeService
        .startProcessInstanceByKey("test_get_certificate_by_name");

    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstance.getId()).list();
    Assertions.assertThat(CollectionUtils.isEmpty(processInstanceList)).isTrue();
  }
}