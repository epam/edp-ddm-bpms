package com.epam.digital.data.platform.bpms.extension.delegate.ceph;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get data from ceph
 * as string using {@link CephService} service.
 */
@Component(GetContentFromCephDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class GetContentFromCephDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "getContentFromCephDelegate";
  private static final String KEY_PARAMETER = "key";
  private static final String CONTENT_PARAMETER = "content";

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    logStartDelegateExecution();
    var key = (String) execution.getVariable(KEY_PARAMETER);

    logProcessExecution("get content by key", key);
    var content = cephService.getContent(cephBucketName, key).orElse(null);

    setTransientResult(execution, CONTENT_PARAMETER, content);
    logDelegateExecution(execution, Set.of(KEY_PARAMETER), Set.of(CONTENT_PARAMETER));
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
