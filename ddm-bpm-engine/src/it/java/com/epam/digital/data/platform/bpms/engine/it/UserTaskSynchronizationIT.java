package com.epam.digital.data.platform.bpms.engine.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.digital.data.platform.bpms.engine.exception.TaskAlreadyInCompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


public class UserTaskSynchronizationIT extends BaseIT{

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
