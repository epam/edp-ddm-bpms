package com.epam.digital.data.platform.bpms.client.config;

import com.epam.digital.data.platform.bpms.client.BaseFeignClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a configuration for all feign clients.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = BaseFeignClient.class)
@EnableFeignClients(basePackageClasses = BaseFeignClient.class)
public class FeignConfig {

}