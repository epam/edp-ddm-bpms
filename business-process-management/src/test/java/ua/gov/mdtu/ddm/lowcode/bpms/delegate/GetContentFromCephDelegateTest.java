package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ua.gov.mdtu.ddm.lowcode.bpms.camunda.delegate.GetContentFromCephDelegate;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.CephService;

@RunWith(MockitoJUnitRunner.class)
public class GetContentFromCephDelegateTest {

  private static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private GetContentFromCephDelegate getContentFromCephDelegate;
  @Mock
  private CephService cephService;
  @Mock
  private DelegateExecution delegateExecution;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(getContentFromCephDelegate, "cephBucketName", CEPH_BUCKET_NAME);
  }

  @Test
  public void execute() {
    when(delegateExecution.getVariable("key")).thenReturn("key");
    when(cephService.getContent(CEPH_BUCKET_NAME, "key")).thenReturn("someContent");

    getContentFromCephDelegate.execute(delegateExecution);

    verify(delegateExecution).setVariableLocal("content",
        Variables.stringValue("someContent", true));
  }
}
