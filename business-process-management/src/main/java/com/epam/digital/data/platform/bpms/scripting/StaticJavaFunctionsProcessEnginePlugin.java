package com.epam.digital.data.platform.bpms.scripting;

import com.epam.digital.data.platform.bpms.scripting.groovy.StaticJavaFunctionsGroovyScriptEnvResolver;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Configuration;

/**
 * Camunda Process Engine Plugin that adds {@link StaticJavaFunctionsGroovyScriptEnvResolver} to
 * process engine configuration
 *
 * @see StaticJavaFunctionsGroovyScriptEnvResolver StaticJavaFunctionsGroovyScriptResolver
 */
@Configuration
@RequiredArgsConstructor
public class StaticJavaFunctionsProcessEnginePlugin extends AbstractProcessEnginePlugin {

  private final StaticJavaFunctionsGroovyScriptEnvResolver staticJavaFunctionsGroovyScriptEnvResolver;

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getEnvScriptResolvers().add(
        staticJavaFunctionsGroovyScriptEnvResolver);
  }
}
