package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.client.dto.SearchSubjectRequestDto;
import com.epam.digital.data.platform.bpms.client.dto.SubjectListDto;
import com.epam.digital.data.platform.bpms.config.TrembitaExchangeGatewayProperties;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SearchSubjectRequest;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.SubjectList;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadClientIdentifierType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadServiceIdentifierType;
import com.epam.digital.data.platform.bpms.mapper.SubjectListMapper;
import com.epam.digital.data.platform.bpms.util.WsObjectFactoryUtil;
import javax.xml.bind.JAXBElement;

/**
 * Client for communication with EDR registry.
 */
public class EdrRegistryClient extends BaseRegistryClient {

  public EdrRegistryClient(XRoadClientIdentifierType client, XRoadServiceIdentifierType service,
      TrembitaExchangeGatewayProperties properties) {
    super(client, service, properties);
  }

  public SubjectListDto searchSubjects(SearchSubjectRequestDto requestDto, String authorizationToken) {
    JAXBElement<SearchSubjectRequest> searchSubjectsRequest = WsObjectFactoryUtil.edrRegistry()
        .createSearchSubjects(SubjectListMapper.INSTANCE.toSearchSubjectRequest(requestDto));
    SubjectList response = sendAndReceive("SearchSubjects", searchSubjectsRequest, authorizationToken);
    return SubjectListMapper.INSTANCE.toSubjectListDto(response);
  }
}
