package com.epam.digital.data.platform.bpms.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.epam.digital.data.platform.bpms.exception.handler.ConnectorResponseErrorHandler;
import com.epam.digital.data.platform.integration.ceph.config.CephConfig;
import com.epam.digital.data.platform.integration.ceph.service.S3ObjectCephService;
import com.epam.digital.data.platform.integration.ceph.service.impl.S3ObjectCephServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.history.HistoricTaskInstanceRestServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.web.client.RestTemplate;

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
  public RestTemplate restTemplate(Collection<ClientHttpRequestInterceptor> interceptors,
      ConnectorResponseErrorHandler responseErrorHandler) {
    return new RestTemplateBuilder()
        .requestFactory(
            () -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .interceptors(interceptors)
        .errorHandler(responseErrorHandler)
        .build();
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

  @Bean
  public S3ObjectCephService s3FileStorageCephService(
      @Value("${ceph.file-storage-bucket}") String cephBucketName,
      AmazonS3 cephAmazonFileStorageS3) {
    return new S3ObjectCephServiceImpl(cephBucketName, cephAmazonFileStorageS3);
  }

  @Bean
  public S3ObjectCephService s3FormDataStorageCephService(
      @Value("${ceph.bucket}") String cephBucketName, AmazonS3 cephAmazonS3) {
    return new S3ObjectCephServiceImpl(cephBucketName, cephAmazonS3);
  }

  @Bean
  public AmazonS3 cephAmazonFileStorageS3(
      @Value("${ceph.http-endpoint}") String cephHttpEndpoint,
      @Value("${ceph.file-storage-access-key}") String cephAccessKey,
      @Value("${ceph.file-storage-secret-key}") String cephSecretKey) {

    var credentials = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(cephAccessKey, cephSecretKey));

    var clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);

    return AmazonS3ClientBuilder.standard()
        .withCredentials(credentials)
        .withClientConfiguration(clientConfig)
        .withEndpointConfiguration(new EndpointConfiguration(cephHttpEndpoint, null))
        .withPathStyleAccessEnabled(true)
        .build();
  }

  @Bean
  public TaskRestService taskRestService(ObjectMapper objectMapper) {
    return new TaskRestServiceImpl(null, objectMapper);
  }

  @Bean
  public HistoricTaskInstanceRestService historicTaskInstanceRestService(ObjectMapper objectMapper,
      ProcessEngine processEngine) {
    return new HistoricTaskInstanceRestServiceImpl(objectMapper, processEngine);
  }
}
