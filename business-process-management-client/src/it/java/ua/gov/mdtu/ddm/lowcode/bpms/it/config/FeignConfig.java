package ua.gov.mdtu.ddm.lowcode.bpms.it.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ua.gov.mdtu.ddm.lowcode.bpms.client.CamundaTaskRestClient;
import ua.gov.mdtu.ddm.lowcode.bpms.client.ProcessDefinitionRestClient;
import ua.gov.mdtu.ddm.lowcode.bpms.client.ProcessInstanceHistoryRestClient;
import ua.gov.mdtu.ddm.lowcode.bpms.client.ProcessInstanceRestClient;

@Configuration
@EnableFeignClients(clients = {
    ProcessDefinitionRestClient.class,
    CamundaTaskRestClient.class,
    ProcessInstanceHistoryRestClient.class,
    ProcessInstanceRestClient.class
})
@EnableAutoConfiguration
public class FeignConfig {

}
