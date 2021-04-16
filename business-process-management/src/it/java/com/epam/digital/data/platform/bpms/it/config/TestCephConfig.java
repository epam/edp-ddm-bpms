package com.epam.digital.data.platform.bpms.it.config;

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.ceph.service.FormDataCephService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestCephConfig {

  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Bean
  @Primary
  public CephService cephService() {
    return new TestCephServiceImpl(cephBucketName);
  }

  @Bean
  @Primary
  public FormDataCephService formDataCephService() {
    return new TestFormDataCephServiceImpl(cephBucketName);
  }
}
