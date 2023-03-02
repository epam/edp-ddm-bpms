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

package com.epam.digital.data.platform.bpm.metrics;

import java.util.function.ToDoubleFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;

@Getter
@RequiredArgsConstructor
public enum DdmBpmSubscriptionsMetric implements DdmBpmMetric {
  ACTIVE_SIGNAL_EVENT_SUBSCRIPTIONS_METRIC("camunda.active.signal.event.subscriptions",
      "The amount of active event subscriptions with type \"signal\"",
      e -> e.getRuntimeService().createEventSubscriptionQuery().eventType("signal").count()),
  ACTIVE_CONDITIONAL_EVENT_SUBSCRIPTIONS_METRIC("camunda.active.conditional.event.subscriptions",
      "The amount of active event subscriptions with type \"conditional\"",
      e -> e.getRuntimeService().createEventSubscriptionQuery().eventType("conditional").count()),
  ACTIVE_COMPENSATE_EVENT_SUBSCRIPTIONS_METRIC("camunda.active.compensate.event.subscriptions",
      "The amount of active event subscriptions with type \"compensate\"",
      e -> e.getRuntimeService().createEventSubscriptionQuery().eventType("compensate").count()),
  ACTIVE_MESSAGE_EVENT_SUBSCRIPTIONS_METRIC("camunda.active.message.event.subscriptions",
      "The amount of active event subscriptions with type \"message\"",
      e -> e.getRuntimeService().createEventSubscriptionQuery().eventType("message").count());


  final String name;
  final String description;
  final ToDoubleFunction<ProcessEngine> metricFunction;
}
