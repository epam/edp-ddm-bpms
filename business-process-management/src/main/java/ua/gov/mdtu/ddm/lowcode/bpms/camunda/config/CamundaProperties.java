package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "camunda")
public class CamundaProperties {

  private Map<String, String> systemVariables;
}
