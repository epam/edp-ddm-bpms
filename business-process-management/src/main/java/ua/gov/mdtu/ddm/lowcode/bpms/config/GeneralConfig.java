package ua.gov.mdtu.ddm.lowcode.bpms.config;

import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.web.client.RestTemplate;
import ua.gov.mdtu.ddm.general.integration.ceph.config.CephConfig;

/**
 * The class represents a holder for beans of the general configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@EnableAspectJAutoProxy
@Import(CephConfig.class)
public class GeneralConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource,
      @Value("${database-startup-validator.interval:10}") int interval,
      @Value("${database-startup-validator.timeout:100}") int timeout) {
    var dsv = new DatabaseStartupValidator();
    dsv.setInterval(interval);
    dsv.setTimeout(timeout);
    dsv.setDataSource(dataSource);
    dsv.setValidationQuery(DatabaseDriver.POSTGRESQL.getValidationQuery());
    return dsv;
  }

  @Bean
  public static BeanFactoryPostProcessor dependsOnPostProcessor() {
    return bf -> {
      String[] jpa = bf.getBeanNamesForType(JpaBaseConfiguration.class);
      Stream.of(jpa)
          .map(bf::getBeanDefinition)
          .forEach(it -> it.setDependsOn("databaseStartupValidator"));
    };
  }
}
