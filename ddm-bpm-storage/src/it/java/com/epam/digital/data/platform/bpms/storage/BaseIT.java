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

import com.epam.digital.data.platform.bpms.storage.config.TestCephServiceImpl;
import com.epam.digital.data.platform.storage.file.service.FormDataFileStorageService;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProvider;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

  @Autowired
  protected RuntimeService runtimeService;
  @Autowired
  protected TaskService taskService;
  @Autowired
  protected TestCephServiceImpl cephService;
  @Autowired
  protected FormDataStorageService formDataStorageService;
  @Autowired
  protected FormDataFileStorageService formDataFileStorageService;
  @Autowired
  protected MessagePayloadStorageService messagePayloadStorageService;
  protected FormDataKeyProvider formDataKeyProvider = new FormDataKeyProviderImpl();

  @Autowired
  protected WireMockServer digitalDocumentService;

  @BeforeEach
  void setup() {
    cephService.clearStorage();
  }
}
