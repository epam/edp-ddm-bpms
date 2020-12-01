package ua.gov.mdtu.ddm.bpms.it.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ua.gov.mdtu.ddm.client.ProcessDefinitionRestClient;

@Configuration
@EnableFeignClients(clients = {
    ProcessDefinitionRestClient.class
})
public class FeignConfig {

}
