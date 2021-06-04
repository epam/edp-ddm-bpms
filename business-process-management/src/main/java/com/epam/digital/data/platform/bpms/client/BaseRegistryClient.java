package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.config.TrembitaExchangeGatewayProperties;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadClientIdentifierType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadServiceIdentifierType;
import javax.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Base Registry Client contains common logic for communication with registries in Trembita using
 * SOAP.
 */
@RequiredArgsConstructor
public class BaseRegistryClient extends WebServiceGatewaySupport {

  private final XRoadClientIdentifierType client;
  private final XRoadServiceIdentifierType service;
  private final TrembitaExchangeGatewayProperties properties;

  protected <T> T sendAndReceive(String serviceCode, Object payload, String authorizationToken) {
    return getResponseBody(
        getWebServiceTemplate().marshalSendAndReceive(payload,
            new TrembitaWsMessageCallback(getMarshaller(), client, service, properties,
                authorizationToken, serviceCode)));
  }

  @SuppressWarnings("unchecked")
  private <T> T getResponseBody(Object response) {
    return ((JAXBElement<T>) response).getValue();
  }

}
