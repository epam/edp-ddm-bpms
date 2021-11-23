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

package com.epam.digital.data.platform.bpms.engine.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The class represents a holder for beans of the jackson configuration. Each method produces a bean
 * and must be annotated with @Bean annotation to be managed by the Spring container. The method
 * should create, set up and return an instance of a bean.
 */
@Configuration
@ComponentScan(basePackages = "com.epam.digital.data.platform.bpms.engine")
public class JacksonConfiguraiton {

  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  @PostConstruct
  public void init() {
    JacksonConfigurator.setDateFormatString(DATE_TIME_FORMAT);
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> builder.simpleDateFormat(DATE_TIME_FORMAT)
        .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
  }
}
