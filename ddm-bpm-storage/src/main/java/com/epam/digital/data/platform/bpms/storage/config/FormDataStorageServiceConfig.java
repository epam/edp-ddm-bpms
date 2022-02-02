package com.epam.digital.data.platform.bpms.storage.config;

import com.epam.digital.data.platform.integration.ceph.config.S3ConfigProperties;
import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.storage.base.config.CephStorageConfiguration;
import com.epam.digital.data.platform.storage.base.factory.StorageServiceFactory;
import com.epam.digital.data.platform.storage.file.service.FormDataFileStorageService;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.storage.message.service.MessagePayloadStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configurations contains beans for form and file data management
 */
@Configuration
public class FormDataStorageServiceConfig {

  @Bean
  @ConfigurationProperties(prefix = "s3.config")
  public S3ConfigProperties s3ConfigProperties() {
    return new S3ConfigProperties();
  }

  @Bean
  public CephS3Factory cephS3Factory() {
    return new CephS3Factory(s3ConfigProperties());
  }

  @Bean
  public StorageServiceFactory storageServiceFactory(ObjectMapper objectMapper,
      CephS3Factory cephS3Factory) {
    return new StorageServiceFactory(objectMapper, cephS3Factory);
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "ceph")
  public FormDataStorageService formDataStorageService(StorageServiceFactory storageServiceFactory,
      CephStorageConfiguration formDataCephStorageConfiguration) {
    return storageServiceFactory.formDataStorageService(formDataCephStorageConfiguration);
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.file-data", name = "type", havingValue = "ceph")
  public FormDataFileStorageService formDataFileStorageService(
      StorageServiceFactory storageServiceFactory,
      CephStorageConfiguration fileDataCephStorageConfiguration) {
    return storageServiceFactory.fromDataFileStorageService(fileDataCephStorageConfiguration);
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "ceph")
  public MessagePayloadStorageService messagePayloadStorageService(
      StorageServiceFactory storageServiceFactory,
      CephStorageConfiguration formDataCephStorageConfiguration) {
    return storageServiceFactory.messagePayloadStorageService(formDataCephStorageConfiguration);
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "ceph")
  @ConfigurationProperties(prefix = "storage.form-data.backend.ceph")
  public CephStorageConfiguration formDataCephStorageConfiguration() {
    return new CephStorageConfiguration();
  }

  @Bean
  @ConditionalOnProperty(prefix = "storage.file-data", name = "type", havingValue = "ceph")
  @ConfigurationProperties(prefix = "storage.file-data.backend.ceph")
  public CephStorageConfiguration fileDataCephStorageConfiguration() {
    return new CephStorageConfiguration();
  }
}
