package com.epam.digital.data.platform.bpms.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.spin.Spin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PutFormDataToCephListenerTest {

  private static final String CEPH_KEY = "cephKey";

  @InjectMocks
  private PutFormDataToCephTaskListener putFormDataToCephTaskListener;
  @Mock
  private FormDataCephService formDataCephService;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private CephKeyProvider cephKeyProvider;
  @Mock
  private DelegateExecution delegateExecution;
  @Mock
  private DelegateTask delegateTask;

  @Captor
  private ArgumentCaptor<FormDataDto> formDataDtoArgumentCaptor;

  @Before
  public void setUp() {
    var taskDefinitionKey = "task";
    var processInstanceId = "id";

    when(delegateTask.getExecution()).thenReturn(delegateExecution);
    when(delegateTask.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
    when(delegateTask.getProcessInstanceId()).thenReturn(processInstanceId);
    when(cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId)).thenReturn(CEPH_KEY);
  }

  @Test
  public void testPutFormDataToCephTaskListener() {
    var map = Map.of("field1", "value1");
    var spinObj = Spin.JSON(map);
    when(delegateExecution.getVariableLocal("userTaskInputFormDataPrepopulate"))
        .thenReturn(spinObj);
    when(objectMapper.convertValue(eq(spinObj.unwrap()), any(TypeReference.class)))
        .thenReturn(new LinkedHashMap<String, Object>(map));

    putFormDataToCephTaskListener.notify(delegateTask);

    verify(formDataCephService).putFormData(eq(CEPH_KEY), formDataDtoArgumentCaptor.capture());
    var formDataDto = formDataDtoArgumentCaptor.getValue();

    assertThat(formDataDto.getData()).hasSize(1).containsAllEntriesOf(map);
  }

  @Test
  public void testPutFormDataToCephTaskListener_noInputParams() {
    putFormDataToCephTaskListener.notify(delegateTask);

    verify(formDataCephService, never()).putFormData(any(), any());
  }

  @Test
  public void testPutFormDataToCephTaskListener_inputParamWithWrongArgumentType() {
    when(delegateExecution.getVariableLocal("userTaskInputFormDataPrepopulate"))
        .thenReturn(new Object());

    putFormDataToCephTaskListener.notify(delegateTask);

    verify(formDataCephService, never()).putFormData(any(), any());
  }
}
