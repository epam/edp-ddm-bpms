package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ua.gov.mdtu.ddm.general.integration.ceph.service.CephService;
import ua.gov.mdtu.ddm.general.integration.ceph.service.FormDataCephService;

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
