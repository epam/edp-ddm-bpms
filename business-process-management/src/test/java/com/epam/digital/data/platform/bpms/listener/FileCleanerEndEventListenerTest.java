package com.epam.digital.data.platform.bpms.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class FileCleanerEndEventListenerTest {

  private static final String PREFIX = "process/processInstanceId/";
  private static final String PROCESS_INSTANCE_ID = "processInstanceId";

  @Mock
  private ExecutionEntity executionEntity;
  @Mock
  private S3ObjectCephService s3ObjectCephService;

  @InjectMocks
  private FileCleanerEndEventListener fileCleanerEndEventListener;

  @Before
  public void setUp() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void shouldReturnEmptyListOfKeysByPrefix() {
    when(executionEntity.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    when(s3ObjectCephService.getKeys(PREFIX)).thenReturn(new ArrayList<>());

    fileCleanerEndEventListener.notify(executionEntity);

    verify(executionEntity, times(1)).getProcessInstanceId();
    verify(s3ObjectCephService, times(1)).getKeys(PREFIX);
    verify(s3ObjectCephService, times(0)).delete(any());
  }

  @Test
  public void shouldDeleteFilesByListOfKeys() {
    var keys = List.of(PREFIX.concat("file1"), PREFIX.concat("file2"));
    when(executionEntity.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    when(s3ObjectCephService.getKeys(PREFIX)).thenReturn(keys);

    fileCleanerEndEventListener.notify(executionEntity);

    verify(executionEntity, times(1)).getProcessInstanceId();
    verify(s3ObjectCephService, times(1)).getKeys(PREFIX);
    verify(s3ObjectCephService, times(1)).delete(keys);
  }
}
