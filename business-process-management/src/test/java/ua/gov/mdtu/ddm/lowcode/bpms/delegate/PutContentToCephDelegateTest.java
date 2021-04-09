package ua.gov.mdtu.ddm.lowcode.bpms.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.lowcode.bpms.delegate.ceph.PutContentToCephDelegate;

@RunWith(MockitoJUnitRunner.class)
public class PutContentToCephDelegateTest {

  private static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private PutContentToCephDelegate putContentToCephDelegate;
  @Mock
  private CephService cephService;
  @Mock
  private ExecutionEntity delegateExecution;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(putContentToCephDelegate, "cephBucketName", CEPH_BUCKET_NAME);
  }

  @Test
  public void execute() {
    when(delegateExecution.getVariable("key")).thenReturn("someKey");
    when(delegateExecution.getVariable("content")).thenReturn("someContent");

    putContentToCephDelegate.execute(delegateExecution);

    verify(cephService).putContent(CEPH_BUCKET_NAME, "someKey", "someContent");
  }

}
