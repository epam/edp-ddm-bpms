package com.epam.digital.data.platform.bpms.listener;

import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove files from ceph before the completion of the business process instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanerEndEventListener implements ExecutionListener {

  private static final String PREFIX_FORMAT = "process/%s/";

  private final S3ObjectCephService s3FileStorageCephService;

  @Override
  public void notify(DelegateExecution execution) {
    var processInstanceId = execution.getProcessInstanceId();
    var prefix = String.format(PREFIX_FORMAT, processInstanceId);
    var keys = s3FileStorageCephService.getKeys(prefix);
    if (!keys.isEmpty()) {
      s3FileStorageCephService.delete(keys);
    }
  }
}
