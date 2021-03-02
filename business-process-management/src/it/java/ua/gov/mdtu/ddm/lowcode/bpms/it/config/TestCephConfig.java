package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;

@Configuration
public class TestCephConfig {

  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Bean
  @Primary
  public CephService cephService() {
    return new TestCephServiceImpl(cephBucketName);
  }
}
