/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.rest.config.mybatis;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class is a configuration for {@link SqlSessionFactory} which is used to execute a custom sql
 * query.
 */
@Configuration
public class SqlSessionFactoryConfig {

  @Bean
  public SqlSessionFactory sqlSessionFactory() {
    var authConfigInputStream = this.getClass()
        .getResourceAsStream("/mybatis/authConfiguration.xml");
    var processEngine = ProcessEngines.getDefaultProcessEngine();
    var processEngineConfig = processEngine.getProcessEngineConfiguration();
    var xmlConfigBuilder = getXmlConfigBuilder(authConfigInputStream, processEngineConfig);
    var configuration = xmlConfigBuilder.parse();
    configuration.setDefaultStatementTimeout(processEngineConfig.getJdbcStatementTimeout());
    return new SqlSessionFactoryBuilder().build(configuration);
  }

  private XMLConfigBuilder getXmlConfigBuilder(InputStream authConfigInputStream,
      ProcessEngineConfiguration processEngineConfig) {
    var dataSource = processEngineConfig.getDataSource();
    var transactionFactory = new ManagedTransactionFactory();
    var environment = new Environment("handler.authorization", transactionFactory, dataSource);
    var xmlConfigBuilder = new XMLConfigBuilder(new InputStreamReader(authConfigInputStream), "",
        getSqlSessionFactoryProperties((ProcessEngineConfigurationImpl) processEngineConfig));
    xmlConfigBuilder.getConfiguration().setEnvironment(environment);
    return xmlConfigBuilder;
  }

  private Properties getSqlSessionFactoryProperties(ProcessEngineConfigurationImpl conf) {
    var properties = new Properties();
    ProcessEngineConfigurationImpl.initSqlSessionFactoryProperties(properties,
        conf.getDatabaseTablePrefix(), conf.getDatabaseType());
    return properties;
  }
}