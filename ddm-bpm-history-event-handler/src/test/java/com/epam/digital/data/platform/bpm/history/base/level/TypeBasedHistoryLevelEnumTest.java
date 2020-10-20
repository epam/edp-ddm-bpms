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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricFormPropertyEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.junit.jupiter.api.Test;

class TypeBasedHistoryLevelEnumTest {

  @Test
  void typeBasedHistoryLevelEnumNone() {
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_NONE
        .isHistoryEventProduced(mock(HistoricProcessInstanceEventEntity.class))).isFalse();
  }

  @Test
  void typeBasedHistoryLevelEnumActivity() {
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricProcessInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricTaskInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricActivityInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricCaseInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricCaseActivityInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_ACTIVITY
        .isHistoryEventProduced(mock(HistoricVariableUpdateEventEntity.class))).isFalse();
  }

  @Test
  void typeBasedHistoryLevelEnumAudit() {
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricProcessInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricTaskInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricActivityInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricCaseInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricCaseActivityInstanceEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricVariableUpdateEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricFormPropertyEventEntity.class))).isTrue();
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_AUDIT
        .isHistoryEventProduced(mock(HistoricIncidentEventEntity.class))).isFalse();
  }

  @Test
  void typeBasedHistoryLevelEnumFull() {
    assertThat(TypeBasedHistoryLevelEnum.HISTORY_LEVEL_FULL.isHistoryEventProduced(null)).isTrue();
  }

  @Test
  void fromName() {
    assertThat(TypeBasedHistoryLevelEnum.fromName("noNE"))
        .isEqualTo(TypeBasedHistoryLevelEnum.NONE);
    assertThat(TypeBasedHistoryLevelEnum.fromName("ACTIVITY"))
        .isEqualTo(TypeBasedHistoryLevelEnum.ACTIVITY);
    assertThat(TypeBasedHistoryLevelEnum.fromName("AuDiT"))
        .isEqualTo(TypeBasedHistoryLevelEnum.AUDIT);
    assertThat(TypeBasedHistoryLevelEnum.fromName("full"))
        .isEqualTo(TypeBasedHistoryLevelEnum.FULL);

    assertThrows(IllegalArgumentException.class,
        () -> TypeBasedHistoryLevelEnum.fromName("no_event"));
  }
}
