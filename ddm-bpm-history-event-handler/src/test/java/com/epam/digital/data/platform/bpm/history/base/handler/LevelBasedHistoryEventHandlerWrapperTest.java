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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.bpm.history.base.level.TypeBasedHistoryLevelEnum;
import java.util.List;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LevelBasedHistoryEventHandlerWrapperTest {

  private LevelBasedHistoryEventHandlerWrapper wrapper;
  @Mock
  private HistoryEventHandler handler;

  @BeforeEach
  void setUp() {
    wrapper = new LevelBasedHistoryEventHandlerWrapper(TypeBasedHistoryLevelEnum.ACTIVITY, handler);
  }

  @Test
  void handleEvents() {
    var variableEvent = mock(HistoricVariableUpdateEventEntity.class);
    var processInstanceEvent = mock(HistoricProcessInstanceEventEntity.class);

    wrapper.handleEvents(List.of(variableEvent, processInstanceEvent));

    verify(handler, never()).handleEvents(any());
    verify(handler, never()).handleEvent(variableEvent);
    verify(handler).handleEvent(processInstanceEvent);
  }
}
