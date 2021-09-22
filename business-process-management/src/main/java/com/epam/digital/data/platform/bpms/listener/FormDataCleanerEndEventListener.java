package com.epam.digital.data.platform.bpms.listener;

import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link ExecutionListener} listener that is used to
 * remove start form and user task form data from ceph before the completion of the business process
 * instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormDataCleanerEndEventListener implements ExecutionListener {

  private static final String USER_TASK_FORM_DATA_PREFIX_FORMAT = "process/%s/";
  private static final String START_FORM_CEPH_KEY = "start_form_ceph_key";

  private final S3ObjectCephService s3FormDataStorageCephService;

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    var startFormDataCephKey = (String) execution.getVariable(START_FORM_CEPH_KEY);
    var processInstanceId = execution.getProcessInstanceId();
    var userFormDataPrefixKey = String.format(USER_TASK_FORM_DATA_PREFIX_FORMAT, processInstanceId);

    try {
      var userFormDataCephKeys = s3FormDataStorageCephService.getKeys(userFormDataPrefixKey);
      if (Objects.nonNull(startFormDataCephKey)) {
        userFormDataCephKeys.add(startFormDataCephKey);
      }
      if (!userFormDataCephKeys.isEmpty()) {
        s3FormDataStorageCephService.delete(userFormDataCephKeys);
      }
    } catch (RuntimeException ex) {
      log.warn(
          "Error while deleting form data from ceph, processDefinitionId={}, processInstanceId={}, userFormDataPrefix={}, startFormDataCephKey={}",
          execution.getProcessDefinitionId(), processInstanceId, userFormDataPrefixKey,
          startFormDataCephKey, ex);
    }
  }
}
