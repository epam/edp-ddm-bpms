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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.bpms.storage.listener.FileCleanerEndEventListener;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.dgtldcmnt.client.DigitalDocumentServiceRestClient;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class FileCleanerEndEventListenerTest {

  private static final String PROCESS_INSTANCE_ID = "processInstanceId";

  @Mock
  private ExecutionEntity executionEntity;
  @Mock
  private DigitalDocumentServiceRestClient client;
  @Mock
  private IdmService idmService;
  @InjectMocks
  private FileCleanerEndEventListener fileCleanerEndEventListener;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  void shouldDeleteFilesByListOfKeys() {
    when(executionEntity.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    when(idmService.getClientAccessToken()).thenReturn("token");

    fileCleanerEndEventListener.notify(executionEntity);

    verify(executionEntity, times(1)).getProcessInstanceId();
    verify(client, times(1)).delete(eq(PROCESS_INSTANCE_ID), any());
  }
}
