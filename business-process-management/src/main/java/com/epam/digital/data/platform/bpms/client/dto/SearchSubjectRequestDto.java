package com.epam.digital.data.platform.bpms.client.dto;

import java.math.BigInteger;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchSubjectRequestDto {

  private String name;
  private String code;
  private String passport;
  private BigInteger limit;
}
