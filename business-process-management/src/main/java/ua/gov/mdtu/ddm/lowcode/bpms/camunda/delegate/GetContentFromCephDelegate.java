package ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.CephService;

@Component
public class GetContentFromCephDelegate implements JavaDelegate {

  private final String cephBucketName;
  private final CephService cephService;

  public GetContentFromCephDelegate(@Value("${ceph.bucket}") String cephBucketName,
      CephService cephService) {
    this.cephBucketName = cephBucketName;
    this.cephService = cephService;
  }

  @Override
  public void execute(DelegateExecution delegateExecution) {
    var key = (String) delegateExecution.getVariable("key");
    var content = Variables.stringValue(cephService.getContent(cephBucketName, key), true);
    delegateExecution.setVariableLocal("content", content);
  }
}
