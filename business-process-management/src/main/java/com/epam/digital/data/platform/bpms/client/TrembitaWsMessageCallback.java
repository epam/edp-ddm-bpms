package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.config.TrembitaExchangeGatewayProperties;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.AuthorizationToken;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadClientIdentifierType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadServiceIdentifierType;
import com.epam.digital.data.platform.bpms.mapper.TrembitaSubsystemPropertiesMapper;
import com.epam.digital.data.platform.bpms.util.WsObjectFactoryUtil;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

/**
 * The class represents an implementation of {@link WebServiceMessageCallback} that is used to add
 * specific headers to SOAP message for communication with Trembita.
 */
@Slf4j
@RequiredArgsConstructor
public class TrembitaWsMessageCallback implements WebServiceMessageCallback {

  private final Marshaller marshaller;
  private final XRoadClientIdentifierType xRoadClient;
  private final XRoadServiceIdentifierType xRoadService;
  private final TrembitaExchangeGatewayProperties properties;
  private final String authorizationToken;
  private final String serviceCode;

  @Override
  public void doWithMessage(WebServiceMessage message) {
    SoapHeader soapHeader = ((SoapMessage) message).getSoapHeader();
    createHeaders().forEach(h -> addAdditionalHeaderToSoapHeader(h, soapHeader));
  }

  private List<JAXBElement> createHeaders() {
    JAXBElement<String> id = WsObjectFactoryUtil.edrRegistry().createId(properties.getId());
    JAXBElement<String> protocolVersion = WsObjectFactoryUtil.edrRegistry()
        .createProtocolVersion(properties.getProtocolVersion());
    AuthorizationToken authToken = new AuthorizationToken();
    authToken.setValue(authorizationToken);
    JAXBElement<AuthorizationToken> authorizationToken = WsObjectFactoryUtil.edrRegistry()
        .createAuthorizationToken(authToken);
    JAXBElement<XRoadClientIdentifierType> client = WsObjectFactoryUtil.edrRegistry()
        .createClient(xRoadClient);
    XRoadServiceIdentifierType xRoadServiceIdentifierType = prepareServiceIdentifier(xRoadService,
        serviceCode);
    JAXBElement<XRoadServiceIdentifierType> service = WsObjectFactoryUtil.edrRegistry()
        .createService(xRoadServiceIdentifierType);

    return List.of(id, protocolVersion, authorizationToken, client, service);
  }

  private XRoadServiceIdentifierType prepareServiceIdentifier(
      XRoadServiceIdentifierType serviceIdentifierType, String serviceCode) {
    XRoadServiceIdentifierType xRoadService = TrembitaSubsystemPropertiesMapper.INSTANCE
        .copy(serviceIdentifierType);
    xRoadService.setServiceCode(serviceCode);
    return xRoadService;
  }

  private <T> void addAdditionalHeaderToSoapHeader(JAXBElement<T> headerToAdd, SoapHeader soapHeader) {
    try {
      marshaller.marshal(headerToAdd, soapHeader.getResult());
    } catch (IOException e) {
      log.error("Error during marshalling of the SOAP headers", e);
    }
  }

}
