package com.epam.digital.data.platform.bpms.storage;

import com.epam.digital.data.platform.bpms.storage.config.TestCephServiceImpl;
import com.epam.digital.data.platform.storage.file.service.FormDataFileStorageService;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  @Autowired
  protected RuntimeService runtimeService;
  @Autowired
  protected TaskService taskService;
  @Autowired
  protected TestCephServiceImpl cephService;
  @Autowired
  protected FormDataStorageService formDataStorageService;
  @Autowired
  protected FormDataFileStorageService formDataFileStorageService;

  @BeforeEach
  void setup() {
    cephService.clearStorage();
  }
}
