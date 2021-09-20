package com.epam.digital.data.platform.bpms.delegate.ceph;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to put data in ceph
 * as string using {@link CephService} service.
 */
@Component(PutContentToCephDelegate.DELEGATE_NAME)
@RequiredArgsConstructor
public class PutContentToCephDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "putContentToCephDelegate";
  private static final String KEY_PARAMETER = "key";
  private static final String CONTENT_PARAMETER = "content";

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    var key = (String) execution.getVariable(KEY_PARAMETER);
    var content = (String) execution.getVariable(CONTENT_PARAMETER);
    cephService.putContent(cephBucketName, key, content);
    logDelegateExecution(execution, Set.of(KEY_PARAMETER, CONTENT_PARAMETER), Set.of());
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }
}
