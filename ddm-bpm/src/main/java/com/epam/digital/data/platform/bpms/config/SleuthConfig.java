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

package com.epam.digital.data.platform.bpms.config;

import brave.baggage.BaggageFields;
import brave.baggage.CorrelationScopeConfig.SingleCorrelationField;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SleuthConfig {

  @Bean
  CurrentTraceContext.ScopeDecorator legacyIds() {
    return MDCScopeDecorator.newBuilder()
        .clear()
          .add(SingleCorrelationField.newBuilder(BaggageFields.TRACE_ID)
            .name("X-B3-TraceId").build())
        .add(SingleCorrelationField.newBuilder(BaggageFields.PARENT_ID)
            .name("X-B3-ParentSpanId").build())
        .add(SingleCorrelationField.newBuilder(BaggageFields.SPAN_ID)
            .name("X-B3-SpanId").build())
        .add(SingleCorrelationField.newBuilder(BaggageFields.SAMPLED)
            .name("X-Span-Export").build())
        .build();
  }
}
