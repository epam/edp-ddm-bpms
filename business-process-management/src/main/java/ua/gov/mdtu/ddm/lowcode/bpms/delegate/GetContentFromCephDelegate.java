package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;

/**
 * The class represents an implementation of {@link JavaDelegate} that is used to get data from ceph
 * as string using {@link CephService} service.
 */
@Component("getContentFromCephDelegate")
@RequiredArgsConstructor
@Logging
public class GetContentFromCephDelegate implements JavaDelegate {

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution execution) {
    var key = (String) execution.getVariable("key");
    var content = cephService.getContent(cephBucketName, key);

    ((AbstractVariableScope) execution).setVariableLocalTransient("content", content);
  }
}
