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

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * Custom history level interface that is used for defining which history event should be published
 * using the default history handler
 */
interface TypeBasedHistoryLevel {

  TypeBasedHistoryLevel HISTORY_LEVEL_NONE = new TypeBasedHistoryLevelNone();
  TypeBasedHistoryLevel HISTORY_LEVEL_ACTIVITY = new TypeBasedHistoryLevelActivity();
  TypeBasedHistoryLevel HISTORY_LEVEL_AUDIT = new TypeBasedHistoryLevelAudit();
  TypeBasedHistoryLevel HISTORY_LEVEL_FULL = new TypeBasedHistoryLevelFull();

  /**
   * Check if {@link HistoryEvent} should be published based on event class type
   *
   * @param event the event to check
   * @return true if the event has to be published
   */
  boolean isHistoryEventProduced(HistoryEvent event);

  /**
   * History level: none. With this level no events should be published using the default history
   * handler
   */
  class TypeBasedHistoryLevelNone implements TypeBasedHistoryLevel {

    @Override
    public boolean isHistoryEventProduced(HistoryEvent event) {
      return false;
    }
  }

  /**
   * History level: activity. With this level only activity events (process-instance, task,
   * activity-instance, case-instance, case-activity-instance) should be published using the default
   * history handler
   */
  class TypeBasedHistoryLevelActivity implements TypeBasedHistoryLevel {

    @Override
    public boolean isHistoryEventProduced(HistoryEvent event) {
      return event instanceof HistoricProcessInstanceEventEntity
          || event instanceof HistoricTaskInstanceEventEntity
          || event instanceof HistoricActivityInstanceEventEntity
          || event instanceof HistoricCaseInstanceEventEntity
          || event instanceof HistoricCaseActivityInstanceEventEntity;
    }
  }

  /**
   * History level: audit. With this level to activity events added variable events
   * (variable-update-event, form-property-event)
   */
  class TypeBasedHistoryLevelAudit extends TypeBasedHistoryLevelActivity
      implements TypeBasedHistoryLevel {

    @Override
    public boolean isHistoryEventProduced(HistoryEvent event) {
      return super.isHistoryEventProduced(event)
          || event instanceof HistoricVariableUpdateEventEntity
          || event instanceof HistoricFormPropertyEventEntity;
    }
  }

  /**
   * History level: full. With this level all events should be published  using the default history
   * handler
   */
  class TypeBasedHistoryLevelFull implements TypeBasedHistoryLevel {

    @Override
    public boolean isHistoryEventProduced(HistoryEvent event) {
      return true;
    }
  }

}
