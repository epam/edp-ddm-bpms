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

package com.epam.digital.data.platform.bpm.history.base.handler;

import com.epam.digital.data.platform.bpm.history.base.level.TypeBasedHistoryLevelEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * A {@link HistoryEventHandler} that is a wrapper over {@link HistoryEventHandler} that delegates
 * the event handling to target based on set history level
 */
@RequiredArgsConstructor
public class LevelBasedHistoryEventHandlerWrapper implements HistoryEventHandler {

  private final TypeBasedHistoryLevelEnum historyLevel;
  private final HistoryEventHandler handler;

  @Override
  public void handleEvent(HistoryEvent historyEvent) {
    if (historyLevel.isHistoryEventProduced(historyEvent)) {
      handler.handleEvent(historyEvent);
    }
  }

  @Override
  public void handleEvents(List<HistoryEvent> historyEvents) {
    historyEvents.forEach(this::handleEvent);
  }
}
