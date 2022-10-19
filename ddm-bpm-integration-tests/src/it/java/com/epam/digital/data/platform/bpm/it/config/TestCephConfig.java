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

package com.epam.digital.data.platform.bpm.it.config;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.storage.file.repository.CephFormDataFileRepository;
import com.epam.digital.data.platform.storage.file.repository.FormDataFileRepository;
import com.epam.digital.data.platform.storage.file.service.FormDataFileKeyProviderImpl;
import com.epam.digital.data.platform.storage.file.service.FormDataFileStorageService;
import com.epam.digital.data.platform.storage.form.repository.CephFormDataRepository;
import com.epam.digital.data.platform.storage.form.repository.FormDataRepository;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.storage.message.repository.CephMessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.repository.MessagePayloadRepository;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadKeyProviderImpl;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestCephConfig {

  @Value("${ceph.bucket}")
  private String cephBucketName;
  @Inject
  private ObjectMapper objectMapper;

  @Bean
  @Primary
  public TestCephServiceImpl cephService() {
    return new TestCephServiceImpl(cephBucketName, objectMapper);
  }

  @Bean
  public FormDataRepository formDataRepository(CephService cephService) {
    return CephFormDataRepository.builder()
        .cephBucketName(cephBucketName)
        .objectMapper(objectMapper)
        .cephService(cephService)
        .build();
  }

  @Bean
  public FormDataFileRepository formDataFileRepository(CephService cephService) {
    return CephFormDataFileRepository.builder()
        .cephBucketName(cephBucketName)
        .cephService(cephService)
        .build();
  }

  @Bean
  public MessagePayloadRepository messagePayloadRepository(CephService cephService) {
    return CephMessagePayloadRepository.builder()
        .cephBucketName(cephBucketName)
        .objectMapper(objectMapper)
        .cephService(cephService)
        .build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "test-ceph")
  public FormDataStorageService formDataStorageService(FormDataRepository formDataRepository) {
    return FormDataStorageService.builder()
        .keyProvider(new FormDataKeyProviderImpl())
        .repository(formDataRepository)
        .build();
  }

  @Bean
  public FormDataFileStorageService formDataFileStorageService(
      FormDataFileRepository formDataFileRepository) {
    return FormDataFileStorageService.builder()
        .keyProvider(new FormDataFileKeyProviderImpl())
        .repository(formDataFileRepository)
        .build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.message-payload", name = "type", havingValue = "test-ceph")
  public MessagePayloadStorageService messagePayloadStorageService(
      MessagePayloadRepository messagePayloadRepository) {
    return MessagePayloadStorageService.builder()
        .keyProvider(new MessagePayloadKeyProviderImpl())
        .repository(messagePayloadRepository)
        .build();
  }
}
