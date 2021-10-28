package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to put data in ceph
 * as string using {@link CephService} service.
 */
@Component(PutContentToCephDelegate.DELEGATE_NAME)
public class PutContentToCephDelegate extends BaseCephDelegate {

  public static final String DELEGATE_NAME = "putContentToCephDelegate";

  public PutContentToCephDelegate(@Value("${ceph.bucket}") String cephBucketName,
      CephService cephService) {
    super(cephBucketName, cephService);
  }

  @Override
  public void executeInternal(DelegateExecution execution) {
    logStartDelegateExecution();
    var key = keyVariable.from(execution).get();
    var content = contentVariable.from(execution).get();
    logProcessExecution("put content with key", key);
    cephService.putContent(cephBucketName, key, content);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
