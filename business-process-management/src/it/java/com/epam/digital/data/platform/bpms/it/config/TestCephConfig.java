package com.epam.digital.data.platform.bpms.it.config;

import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Value;
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
  @Primary
  public S3ObjectCephService s3ObjectCephServiceTest() {
    return new TestS3ObjectCephServiceImpl();
  }
}
