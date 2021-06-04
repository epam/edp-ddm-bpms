package com.epam.digital.data.platform.bpms.mapper;

import com.epam.digital.data.platform.bpms.client.dto.ErrorsPartDto;
import com.epam.digital.data.platform.bpms.client.dto.SearchSubjectRequestDto;
import com.epam.digital.data.platform.bpms.client.dto.SubjectInfoArrayDto;
import com.epam.digital.data.platform.bpms.client.dto.SubjectInfoDto;
import com.epam.digital.data.platform.bpms.client.dto.SubjectListDto;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.ErrorsPart;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SearchSubjectRequest;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SubjectInfo;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SubjectInfoArray;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SubjectList;
import com.epam.digital.data.platform.bpms.util.WsObjectFactoryUtil;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectListMapper {

  SubjectListMapper INSTANCE = Mappers.getMapper(SubjectListMapper.class);

  @Mapping(target = "subjectList", source = "subjectList.value", qualifiedByName = "toSubjectInfoArrayDto")
  @Mapping(target = "errors", source = "errors.value", qualifiedByName = "toErrorsPartDto")
  SubjectListDto toSubjectListDto(SubjectList subjectList);

  @Named("toSubjectInfoArrayDto")
  @Mapping(target = "subjectInfo", qualifiedByName = "toSubjectInfoDtoList")
  SubjectInfoArrayDto toSubjectInfoArrayDto(SubjectInfoArray subjectInfoArray);

  @Named("toErrorsPartDto")
  @Mapping(target = "innerMessage", source = "innerMessage.value")
  @Mapping(target = "innerCode", source = "innerCode.value")
  @Mapping(target = "message", source = "message.value")
  @Mapping(target = "code", source = "code.value")
  ErrorsPartDto toErrorsPartDto(ErrorsPart errorsPart);

  @Named("toSubjectInfoDto")
  @Mapping(target = "state", source = "state.value")
  @Mapping(target = "stateText", source = "stateText.value")
  @Mapping(target = "name", source = "name.value")
  @Mapping(target = "url", source = "url.value")
  @Mapping(target = "code", source = "code.value")
  @Mapping(target = "id", source = "id.value")
  SubjectInfoDto toSubjectInfoDto(SubjectInfo subjectInfo);

  @Named("toSubjectInfoDtoList")
  @IterableMapping(qualifiedByName = "toSubjectInfoDto")
  List<SubjectInfoDto> toSubjectInfoDtoList(List<SubjectInfo> subjectInfoList);

  default SearchSubjectRequest toSearchSubjectRequest(SearchSubjectRequestDto requestDto) {
    SearchSubjectRequest searchSubjectRequest = WsObjectFactoryUtil.edrRegistry()
        .createSearchSubjectRequest();
    searchSubjectRequest.setName(
        WsObjectFactoryUtil.edrRegistry().createSearchSubjectRequestName(requestDto.getName()));
    searchSubjectRequest.setPassport(WsObjectFactoryUtil.edrRegistry()
        .createSearchSubjectRequestPassport(requestDto.getPassport()));
    searchSubjectRequest.setCode(
        WsObjectFactoryUtil.edrRegistry().createSearchSubjectRequestCode(requestDto.getCode()));
    searchSubjectRequest.setLimit(
        WsObjectFactoryUtil.edrRegistry().createSearchSubjectRequestLimit(requestDto.getLimit()));
    return searchSubjectRequest;
  }
}
