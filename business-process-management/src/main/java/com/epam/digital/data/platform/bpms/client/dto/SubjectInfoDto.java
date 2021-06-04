package com.epam.digital.data.platform.bpms.client.dto;

import java.math.BigInteger;
import lombok.Data;

@Data
public class SubjectInfoDto {

  private BigInteger state;
  private String stateText;
  private String name;
  private String url;
  private String code;
  private BigInteger id;
}
