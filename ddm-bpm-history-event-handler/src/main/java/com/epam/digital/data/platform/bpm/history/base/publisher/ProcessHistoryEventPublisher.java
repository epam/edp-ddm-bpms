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

package com.epam.digital.data.platform.bpm.history.base.publisher;

import com.epam.digital.data.platform.bpm.history.base.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpm.history.base.dto.HistoryTaskDto;

/**
 * Publisher that is used to publish messages to a message broker
 */
public interface ProcessHistoryEventPublisher {

  /**
   * Publish put (create or update) request of the {@link HistoryProcessInstanceDto}
   *
   * @param dto the dto to be published
   */
  void put(HistoryProcessInstanceDto dto);

  /**
   * Publish patch (partial update) request of the {@link HistoryProcessInstanceDto}
   *
   * @param dto the dto to be published
   */
  void patch(HistoryProcessInstanceDto dto);

  /**
   * Publish put (create or update) request of the {@link HistoryTaskDto}
   *
   * @param dto the dto to be published
   */
  void put(HistoryTaskDto dto);

  /**
   * Publish patch (partial update) request of the {@link HistoryTaskDto}
   *
   * @param dto the dto to be published
   */
  void patch(HistoryTaskDto dto);
}
