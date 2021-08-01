package com.epam.digital.data.platform.bpms.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.delegate.ceph.GetContentFromCephDelegate;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import java.util.Optional;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class GetContentFromCephDelegateTest {

  private static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private GetContentFromCephDelegate getContentFromCephDelegate;
  @Mock
  private CephService cephService;
  @Mock
  private ExecutionEntity delegateExecution;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(getContentFromCephDelegate, "cephBucketName", CEPH_BUCKET_NAME);
  }

  @Test
  public void execute() {
    when(delegateExecution.getVariable("key")).thenReturn("key");
    when(cephService.getContent(CEPH_BUCKET_NAME, "key")).thenReturn(Optional.of("someContent"));

    getContentFromCephDelegate.execute(delegateExecution);

    verify(delegateExecution).setVariableLocalTransient("content", "someContent");
  }
}
