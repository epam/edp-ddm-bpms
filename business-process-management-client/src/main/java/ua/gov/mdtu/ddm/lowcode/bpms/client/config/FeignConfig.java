package ua.gov.mdtu.ddm.lowcode.bpms.client.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ua.gov.mdtu.ddm.lowcode.bpms.client.BaseFeignClient;

/**
 * The class represents a configuration for all feign clients.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = BaseFeignClient.class)
@EnableFeignClients(basePackageClasses = BaseFeignClient.class)
public class FeignConfig {

}