package ua.gov.mdtu.ddm.lowcode.bpms.it;


import javax.inject.Inject;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public abstract class BaseIT {

  @Inject
  protected RuntimeService runtimeService;
  @Inject
  protected HistoryService historyService;
  @Inject
  protected TaskService taskService;
  @Inject
  protected ProcessEngine engine;

}
