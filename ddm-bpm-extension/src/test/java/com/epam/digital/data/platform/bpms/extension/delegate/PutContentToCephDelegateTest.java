package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.PutContentToCephDelegate;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PutContentToCephDelegateTest {

  private static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private PutContentToCephDelegate putContentToCephDelegate;
  @Mock
  private CephService cephService;
  @Mock
  private ExecutionEntity delegateExecution;

  @Mock
  private NamedVariableAccessor<String> keyVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> keyVariableReadAccessor;

  @Mock
  private NamedVariableAccessor<String> contentVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> contentVariableReadAccessor;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(putContentToCephDelegate, "cephBucketName", CEPH_BUCKET_NAME);
    ReflectionTestUtils.setField(putContentToCephDelegate, "keyVariable", keyVariableAccessor);
    ReflectionTestUtils.setField(putContentToCephDelegate, "contentVariable",
        contentVariableAccessor);

    when(keyVariableAccessor.from(delegateExecution)).thenReturn(keyVariableReadAccessor);
    when(contentVariableAccessor.from(delegateExecution)).thenReturn(contentVariableReadAccessor);
  }

  @Test
  public void execute() throws Exception {
    when(keyVariableReadAccessor.get()).thenReturn("someKey");
    when(contentVariableReadAccessor.get()).thenReturn("someContent");

    putContentToCephDelegate.execute(delegateExecution);

    verify(cephService).putContent(CEPH_BUCKET_NAME, "someKey", "someContent");
  }

}
