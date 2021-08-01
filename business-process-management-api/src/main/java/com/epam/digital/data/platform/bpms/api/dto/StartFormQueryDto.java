package com.epam.digital.data.platform.bpms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class represents a data transfer object for building query to get process-definition
 * start-forms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartFormQueryDto {

  private List<String> processDefinitionIdIn;

}
