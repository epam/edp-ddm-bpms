package ua.gov.mdtu.ddm.lowcode.bpms.camunda.config.el;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CamundaConfiguration {

  private final ApplicationContext appContext;

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(
      List<ProcessEnginePlugin> processEnginePlugins) {
    var configuration = CamundaSpringBootUtil.springProcessEngineConfiguration();
    configuration.getProcessEnginePlugins()
        .add(new CompositeProcessEnginePlugin(processEnginePlugins));

    var expressionManager = new CamundaSpringExpressionManager(appContext,
        configuration.getBeans());
    configuration.setExpressionManager(expressionManager);
    return configuration;
  }
}
