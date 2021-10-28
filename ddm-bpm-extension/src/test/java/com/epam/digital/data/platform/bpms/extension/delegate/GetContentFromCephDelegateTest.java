package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.GetContentFromCephDelegate;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableWriteAccessor;
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

  @Mock
  private NamedVariableAccessor<String> keyVariableAccessor;
  @Mock
  private NamedVariableReadAccessor<String> keyVariableReadAccessor;

  @Mock
  private NamedVariableAccessor<String> contentVariableAccessor;
  @Mock
  private NamedVariableWriteAccessor<String> contentVariableWriteAccessor;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(getContentFromCephDelegate, "cephBucketName", CEPH_BUCKET_NAME);
    ReflectionTestUtils.setField(getContentFromCephDelegate, "keyVariable", keyVariableAccessor);
    ReflectionTestUtils.setField(getContentFromCephDelegate, "contentVariable",
        contentVariableAccessor);

    when(keyVariableAccessor.from(delegateExecution)).thenReturn(keyVariableReadAccessor);
    when(contentVariableAccessor.on(delegateExecution)).thenReturn(contentVariableWriteAccessor);
  }

  @Test
  public void execute() throws Exception {
    when(keyVariableReadAccessor.get()).thenReturn("key");
    when(cephService.getContent(CEPH_BUCKET_NAME, "key")).thenReturn(Optional.of("someContent"));

    getContentFromCephDelegate.execute(delegateExecution);

    verify(contentVariableWriteAccessor).set("someContent");
  }
}
