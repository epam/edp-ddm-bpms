package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.starter.logger.annotation.Logging;

@Component("putContentToCephDelegate")
@RequiredArgsConstructor
@Logging
public class PutContentToCephDelegate implements JavaDelegate {

  @Value("${ceph.bucket}")
  private final String cephBucketName;
  private final CephService cephService;

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var key = (String) delegateExecution.getVariable("key");
    var content = (String) delegateExecution.getVariable("content");
    cephService.putContent(cephBucketName, key, content);
  }
}
