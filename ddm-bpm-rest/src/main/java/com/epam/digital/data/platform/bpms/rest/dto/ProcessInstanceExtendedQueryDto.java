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

package com.epam.digital.data.platform.bpms.rest.dto;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceQueryDto.SortByConstants;
import com.epam.digital.data.platform.bpms.api.dto.enums.SortOrder;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * Extended Camunda {@link ProcessInstanceQueryDto} with a possibility of custom {@link
 * DdmProcessInstanceDto process-instance} sorting
 */
public class ProcessInstanceExtendedQueryDto extends ProcessInstanceQueryDto {

  private static final Set<String> DEFAULT_SORT_BY_VALUES = Set.of(
      SortByConstants.SORT_BY_DEFINITION_ID,
      SortByConstants.SORT_BY_BUSINESS_KEY,
      SortByConstants.SORT_BY_DEFINITION_KEY,
      SortByConstants.SORT_BY_INSTANCE_ID,
      SortByConstants.SORT_BY_TENANT_ID);

  private static final Map<String, Comparator<DdmProcessInstanceDto>> EXTENDED_SORT_BY_COMPARATORS =
      Map.of(
          SortByConstants.SORT_BY_START_TIME,
          Comparator.comparing(DdmProcessInstanceDto::getStartTime),
          SortByConstants.SORT_BY_DEFINITION_NAME,
          Comparator.comparing(DdmProcessInstanceDto::getProcessDefinitionName));

  @Override
  protected boolean isValidSortByValue(String value) {
    return Objects.isNull(value) || DEFAULT_SORT_BY_VALUES.contains(value) ||
        EXTENDED_SORT_BY_COMPARATORS.containsKey(value);
  }

  @Override
  protected void applySortingOptions(ProcessInstanceQuery query, ProcessEngine engine) {
    if (Objects.isNull(sortBy) || DEFAULT_SORT_BY_VALUES.contains(sortBy)) {
      super.applySortingOptions(query, engine);
    }
  }

  /**
   * Return custom comparator if {@link ProcessInstanceQueryDto#sortBy} contains a value other than
   * default value
   *
   * @return custom comparator
   */
  public Comparator<DdmProcessInstanceDto> getCustomComparator() {
    if (Objects.isNull(sortBy)) {
      return null;
    }
    var comparator = EXTENDED_SORT_BY_COMPARATORS.get(sortBy);
    if (Objects.nonNull(comparator) && SortOrder.DESC.stringValue().equals(sortOrder)) {
      comparator = comparator.reversed();
    }
    return comparator;
  }
}
