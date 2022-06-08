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

package com.epam.digital.data.platform.bpms.extension.delegate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.extension.delegate.storage.PutContentToCephDelegate;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableReadAccessor;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PutContentToCephDelegateTest {

  private static final String CEPH_BUCKET_NAME = "cephBucket";

  @InjectMocks
  private PutContentToCephDelegate putContentToCephDelegate;
  @Mock
  private FormDataStorageService formDataStorageService;
  @Mock
  private ExecutionEntity delegateExecution;
  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

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
    when(contentVariableReadAccessor.get()).thenReturn("{\"data\": {}}");

    putContentToCephDelegate.execute(delegateExecution);

    verify(formDataStorageService).putFormData(eq("someKey"), any(FormDataDto.class));
  }

}
