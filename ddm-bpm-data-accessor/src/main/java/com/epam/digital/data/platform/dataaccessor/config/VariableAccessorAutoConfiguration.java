/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.dataaccessor.config;

import com.epam.digital.data.platform.dataaccessor.BaseVariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariableBeanPostProcessor;
import com.epam.digital.data.platform.dataaccessor.completer.BaseCompleterVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.completer.CompleterVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.initiator.BaseInitiatorVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.initiator.InitiatorVariablesAccessor;
import com.epam.digital.data.platform.dataaccessor.named.BaseNamedVariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessorFactory;
import com.epam.digital.data.platform.dataaccessor.sysvar.CallerProcessInstanceIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessCompletionResultVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.ProcessExcerptIdVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartFormCephKeyVariable;
import com.epam.digital.data.platform.dataaccessor.sysvar.StartMessagePayloadStorageKeyVariable;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Auto-configuration that will be created only in Camunda Spring context (exists {@link
 * ProcessEngineConfiguration} bean in context)
 * <p>
 * Creates {@link NamedVariableAccessorFactory} and {@link SystemVariableBeanPostProcessor} beans
 * (for annotation-driven variable accessing) if there is no such beans in Spring context
 */
@Configuration
@AutoConfigureOrder(value = Ordered.LOWEST_PRECEDENCE)
@ConditionalOnBean(ProcessEngineConfiguration.class)
public class VariableAccessorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(NamedVariableAccessorFactory.class)
  public NamedVariableAccessorFactory variableAccessorFactory(
      VariableAccessorFactory variableAccessorFactory) {
    return new BaseNamedVariableAccessorFactory(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(SystemVariableBeanPostProcessor.class)
  public SystemVariableBeanPostProcessor variableBeanPostProcessor(
      NamedVariableAccessorFactory namedVariableAccessorFactory) {
    return new SystemVariableBeanPostProcessor(namedVariableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(VariableAccessorFactory.class)
  public VariableAccessorFactory baseVariableAccessorFactory() {
    return new BaseVariableAccessorFactory();
  }

  @Bean
  @ConditionalOnMissingBean(StartFormCephKeyVariable.class)
  public StartFormCephKeyVariable startFormCephKeyVariable(
      VariableAccessorFactory variableAccessorFactory) {
    return new StartFormCephKeyVariable(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(StartMessagePayloadStorageKeyVariable.class)
  public StartMessagePayloadStorageKeyVariable startMessagePayloadStorageKey(
      VariableAccessorFactory variableAccessorFactory) {
    return new StartMessagePayloadStorageKeyVariable(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(CallerProcessInstanceIdVariable.class)
  public CallerProcessInstanceIdVariable callerProcessInstanceIdVariable(
      VariableAccessorFactory variableAccessorFactory) {
    return new CallerProcessInstanceIdVariable(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(ProcessCompletionResultVariable.class)
  public ProcessCompletionResultVariable processCompletionResultVariable(
      VariableAccessorFactory variableAccessorFactory) {
    return new ProcessCompletionResultVariable(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(ProcessExcerptIdVariable.class)
  public ProcessExcerptIdVariable processExcerptIdVariable(
      VariableAccessorFactory variableAccessorFactory) {
    return new ProcessExcerptIdVariable(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(CompleterVariablesAccessor.class)
  public CompleterVariablesAccessor completerVariablesAccessorFactory(
      VariableAccessorFactory variableAccessorFactory) {
    return new BaseCompleterVariablesAccessor(variableAccessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean(InitiatorVariablesAccessor.class)
  public InitiatorVariablesAccessor initiatorVariablesAccessor(
      VariableAccessorFactory variableAccessorFactory) {
    return new BaseInitiatorVariablesAccessor(variableAccessorFactory);
  }
}
