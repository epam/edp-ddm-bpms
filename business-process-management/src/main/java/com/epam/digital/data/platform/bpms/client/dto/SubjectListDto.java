package com.epam.digital.data.platform.bpms.client.dto;

import lombok.Data;

@Data
public class SubjectListDto {

  private SubjectInfoArrayDto subjectList;
  private ErrorsPartDto errors;

}
