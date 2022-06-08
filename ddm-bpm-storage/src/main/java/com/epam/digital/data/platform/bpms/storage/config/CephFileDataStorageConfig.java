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

package com.epam.digital.data.platform.bpms.storage.config;

import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.storage.file.config.FileDataCephStorageConfiguration;
import com.epam.digital.data.platform.storage.file.factory.FormDataFileStorageServiceFactory;
import com.epam.digital.data.platform.storage.file.service.FormDataFileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "storage.file-data", name = "type", havingValue = "ceph")
public class CephFileDataStorageConfig {

  @Bean
  public FormDataFileStorageServiceFactory fileStorageServiceFactory(ObjectMapper objectMapper,
      CephS3Factory cephS3Factory) {
    return new FormDataFileStorageServiceFactory(cephS3Factory);
  }

  @Bean
  public FormDataFileStorageService formDataFileStorageService(
      FormDataFileStorageServiceFactory fileStorageServiceFactory,
      FileDataCephStorageConfiguration fileDataCephStorageConfiguration) {
    return fileStorageServiceFactory.fromDataFileStorageService(fileDataCephStorageConfiguration);
  }

  @Bean
  @ConfigurationProperties(prefix = "storage.file-data.backend.ceph")
  public FileDataCephStorageConfiguration fileDataCephStorageConfiguration() {
    return new FileDataCephStorageConfiguration();
  }
}
