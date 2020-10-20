package com.epam.digital.data.platform.bpm.it.config;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class ScopesConfig {

  @Bean
  public CustomScopeConfigurer customScopeConfigurer() {
    var scopeConfigurer = new CustomScopeConfigurer();

    scopeConfigurer.addScope(WebApplicationContext.SCOPE_REQUEST, new SimpleThreadScope());

    return scopeConfigurer;
  }
}
