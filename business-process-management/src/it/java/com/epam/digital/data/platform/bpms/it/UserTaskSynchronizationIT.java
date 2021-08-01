package com.epam.digital.data.platform.bpms.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.exception.TaskAlreadyInCompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class UserTaskSynchronizationIT extends BaseIT {

  @Test
  @Deployment(resources = {"bpmn/testSynchronization.bpmn"})
  public void testUserTaskSynchronization() throws InterruptedException {
    var executorService = Executors.newFixedThreadPool(2);

    var processInstance = runtimeService.startProcessInstanceByKey("testSynchronization");

    var taskId = taskService.createTaskQuery()
        .processInstanceId(processInstance.getProcessInstanceId()).singleResult().getId();

    executorService.submit(() -> taskService.complete(taskId));
    Thread.sleep(100);
    var resultFuture = executorService.submit(() -> taskService.complete(taskId));

    var ex = assertThrows(ExecutionException.class, resultFuture::get);

    assertThat(ex.getCause()).isInstanceOf(TaskAlreadyInCompletionException.class);
    assertThat(ex.getCause().getMessage()).isEqualTo("Task " + taskId + " already in completion");
  }
}
