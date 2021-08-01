package com.epam.digital.data.platform.bpms.scripting.groovy;

import java.util.Objects;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.commons.utils.IoUtil;
import org.springframework.stereotype.Component;

/**
 * Resolver for static java functions for groovy scripts
 *
 * @see com.epam.digital.data.platform.bpms.scripting.StaticJavaFunctionsProcessEnginePlugin
 * StaticJavaFunctionsProcessEnginePlugin
 * @see "/groovy/importStaticJavaFunctions.groovy"
 */
@Component
public class StaticJavaFunctionsGroovyScriptEnvResolver implements ScriptEnvResolver {

  private static final String GROOVY = "groovy";
  private static final String SCRIPT_ENV_PATH = "/groovy/importStaticJavaFunctions.groovy";

  @Override
  public String[] resolve(String language) {
    if (!GROOVY.equalsIgnoreCase(language)) {
      return null;
    }

    var envResource = Objects.requireNonNull(getClass().getResourceAsStream(SCRIPT_ENV_PATH),
        String.format("No script env %s found for language %s", SCRIPT_ENV_PATH, GROOVY));
    try {
      return new String[]{IoUtil.inputStreamAsString(envResource)};
    } finally {
      IoUtil.closeSilently(envResource);
    }
  }
}
