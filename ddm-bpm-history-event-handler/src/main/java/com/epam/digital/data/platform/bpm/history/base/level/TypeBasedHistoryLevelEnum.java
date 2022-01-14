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

package com.epam.digital.data.platform.bpm.history.base.level;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * History level enum that is used for defining which history event should be published using the
 * default history handler
 *
 * @see TypeBasedHistoryLevel
 */
@RequiredArgsConstructor
public enum TypeBasedHistoryLevelEnum implements TypeBasedHistoryLevel {
  NONE(ProcessEngineConfiguration.HISTORY_NONE, TypeBasedHistoryLevel.HISTORY_LEVEL_NONE),
  ACTIVITY(ProcessEngineConfiguration.HISTORY_ACTIVITY, TypeBasedHistoryLevel.HISTORY_LEVEL_ACTIVITY),
  AUDIT(ProcessEngineConfiguration.HISTORY_AUDIT, TypeBasedHistoryLevel.HISTORY_LEVEL_AUDIT),
  FULL(ProcessEngineConfiguration.HISTORY_FULL, TypeBasedHistoryLevel.HISTORY_LEVEL_FULL);

  @Getter
  private final String name;
  private final TypeBasedHistoryLevel level;

  @Override
  public boolean isHistoryEventProduced(HistoryEvent event) {
    return level.isHistoryEventProduced(event);
  }

  public static TypeBasedHistoryLevelEnum fromName(String name) {
    return Arrays.stream(values())
        .filter(levelEnum -> levelEnum.getName().equalsIgnoreCase(name))
        .reduce((levelEnum, levelEnum2) -> {
          throw new IllegalStateException("History levels must have different names");
        })
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("History level %s doesn't exist", name)));
  }
}
