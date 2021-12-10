/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.bpms.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.storage.listener.PutFormDataToStorageTaskListener;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
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

  @InjectMocks
  private PutFormDataToStorageTaskListener putFormDataToStorageTaskListener;
  @Mock
  private FormDataStorageService formDataStorageService;
  @Mock
  private ObjectMapper objectMapper;
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
    ReflectionTestUtils.setField(putFormDataToStorageTaskListener,
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

    putFormDataToStorageTaskListener.notify(delegateTask);

    verify(formDataStorageService).putFormData(eq(taskDefinitionKey), eq(processInstanceId),
        formDataDtoArgumentCaptor.capture());
    var formDataDto = formDataDtoArgumentCaptor.getValue();

    assertThat(formDataDto.getData()).hasSize(1).containsAllEntriesOf(map);
  }

  @Test
  void testPutFormDataToCephTaskListener_noInputParams() {
    putFormDataToStorageTaskListener.notify(delegateTask);

    verify(formDataStorageService, never()).putFormData(any(), any());
  }
}
