package com.epam.digital.data.platform.bpms.client.dto;

import java.math.BigInteger;
import lombok.Data;

@Data
public class ErrorsPartDto {

  private String innerMessage;
  private BigInteger innerCode;
  private String message;
  private BigInteger code;
}
