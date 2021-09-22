package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;

public class FileCleanerEndEventListenerIT extends BaseIT {

  @Test
  @Deployment(resources = "bpmn/file_cleaner_listener.bpmn")
  public void testFileCleanerEndEventListener() {
    var contentType = "application/pdf";
    var userMetadata = new HashMap<String, String>();
    var data = new ByteArrayInputStream(new byte[]{1});

    var processInstance = runtimeService.startProcessInstanceByKey("fileCleanerListenerKey");
    var processInstanceId = processInstance.getId();
    var taskId = taskService.createTaskQuery().taskDefinitionKey("fileCleanerListenerId")
        .singleResult().getId();
    var key = generateCephFileKey(processInstanceId, "file1.pdf");
    var key2 = generateCephFileKey(processInstanceId, "file2.pdf");
    cephService.put(key, contentType, userMetadata, data);
    cephService.put(key2, contentType, userMetadata, data);

    assertThat(cephService.getStorage()).hasSize(2);

    taskService.complete(taskId);

    BpmnAwareTests.assertThat(processInstance).isEnded();
    assertThat(cephService.getStorage()).isEmpty();
  }

  private String generateCephFileKey(String processInstanceId, String fileName) {
    return String.format("process/%s/%s", processInstanceId, fileName);
  }
}
