package ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ua.gov.mdtu.ddm.lowcode.bpms.integration.ceph.service.impl.CephServiceS3Impl;

@Configuration
@ComponentScan(basePackageClasses = CephServiceS3Impl.class)
public class CephConfig {

  @Bean
  public AmazonS3 cephAmazonS3(
      @Value("${ceph.http-endpoint}") String cephHttpEndpoint,
      @Value("${ceph.access-key}") String cephAccessKey,
      @Value("${ceph.secret-key}") String cephSecretKey) {

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

}
