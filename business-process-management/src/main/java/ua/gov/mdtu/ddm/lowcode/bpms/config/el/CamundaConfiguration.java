package ua.gov.mdtu.ddm.lowcode.bpms.config.el;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the camunda configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@RequiredArgsConstructor
public class CamundaConfiguration {

  private final ApplicationContext appContext;
  private final LowcodeSpringProcessEngineConfiguration configuration;

  @Bean
  public ProcessEngineConfigurationImpl processEngineConfigurationImpl(
      List<ProcessEnginePlugin> processEnginePlugins) {
    CamundaSpringBootUtil.initCustomFields(this.configuration);
    configuration.getProcessEnginePlugins()
        .add(new CompositeProcessEnginePlugin(processEnginePlugins));

    var expressionManager = new CamundaSpringExpressionManager(appContext,
        configuration.getBeans());
    configuration.setExpressionManager(expressionManager);
    return configuration;
  }
}
