package com.epam.digital.data.platform.bpms.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.ceph.CephKeyProvider;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.integration.ceph.dto.FormDataDto;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PutFormDataToCephListenerTest {

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
  @Mock
  private NamedVariableAccessor<SpinJsonNode> userTaskInputFormDataPrepopulateVariable;
  @Mock
  private NamedVariableReadAccessor<SpinJsonNode> userTaskInputFormDataPrepopulateReadAccessor;

  @Captor
  private ArgumentCaptor<FormDataDto> formDataDtoArgumentCaptor;

  @BeforeEach
  void setUp() {
    when(delegateTask.getExecution()).thenReturn(delegateExecution);

    when(userTaskInputFormDataPrepopulateVariable.from(delegateExecution)).thenReturn(
        userTaskInputFormDataPrepopulateReadAccessor);
    ReflectionTestUtils.setField(putFormDataToCephTaskListener,
        "userTaskInputFormDataPrepopulateVariable", userTaskInputFormDataPrepopulateVariable);
  }

  @Test
  void testPutFormDataToCephTaskListener() {
    var taskDefinitionKey = "task";
    var processInstanceId = "id";
    var map = Map.of("field1", "value1");
    var spinObj = Spin.JSON(map);
    when(userTaskInputFormDataPrepopulateReadAccessor.get()).thenReturn(spinObj);
    when(objectMapper.convertValue(eq(spinObj.unwrap()), any(TypeReference.class)))
        .thenReturn(new LinkedHashMap<String, Object>(map));

    when(delegateTask.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
    when(delegateTask.getProcessInstanceId()).thenReturn(processInstanceId);
    when(cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId)).thenReturn(CEPH_KEY);

    putFormDataToCephTaskListener.notify(delegateTask);

    verify(formDataCephService).putFormData(eq(CEPH_KEY), formDataDtoArgumentCaptor.capture());
    var formDataDto = formDataDtoArgumentCaptor.getValue();

    assertThat(formDataDto.getData()).hasSize(1).containsAllEntriesOf(map);
  }

  @Test
  void testPutFormDataToCephTaskListener_noInputParams() {
    putFormDataToCephTaskListener.notify(delegateTask);

    verify(formDataCephService, never()).putFormData(any(), any());
  }
}
