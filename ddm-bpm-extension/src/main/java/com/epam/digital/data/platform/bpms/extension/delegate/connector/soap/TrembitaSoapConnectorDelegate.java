/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.soap;

import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_NAMESPACE_IDENTIFIERS;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_NAMESPACE_XROAD_XSD;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PREFIX_IDEN;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PREFIX_XRO;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_CLIENT;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_ID;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_MEMBER_CLASS;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_MEMBER_CODE;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_OBJECT_TYPE;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_PROTOCOL_VERSION;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SERVICE;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SERVICE_CAPS;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SERVICE_CODE;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SERVICE_VERSION;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SUBSYSTEM;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_SUBSYSTEM_CODE;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_USER_ID;
import static com.epam.digital.data.platform.bpms.extension.config.properties.TrembitaConstants.TREMBITA_PROPERTY_X_ROAD_INSTANCE;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.starter.trembita.integration.base.config.RegistryProperties;
import com.epam.digital.data.platform.starter.trembita.integration.base.config.TrembitaExchangeGatewayProperties;
import com.epam.digital.data.platform.starter.trembita.integration.base.config.TrembitaSubsystemProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.connect.httpclient.soap.SoapHttpConnector;
import org.camunda.spin.Spin;
import org.camunda.spin.xml.SpinXmlElement;
import org.springframework.stereotype.Component;

/**
 * The class represents an implementation of {@link BaseJavaDelegate} that is used for connecting to
 * trembita registries using {@link SoapHttpConnector}
 */
@Slf4j
@Component(TrembitaSoapConnectorDelegate.DELEGATE_NAME)
public class TrembitaSoapConnectorDelegate extends BaseJavaDelegate {

  public static final String DELEGATE_NAME = "trembitaSoapConnectorDelegate";

  private final SoapHttpConnector soapHttpConnector;
  private final TrembitaExchangeGatewayProperties trembitaExchangeGatewayProperties;

  @SystemVariable(name = "systemName")
  private NamedVariableAccessor<String> systemNameVariable;
  @SystemVariable(name = "trembitaSoapAction")
  private NamedVariableAccessor<String> trembitaSoapActionVariable;
  @SystemVariable(name = "payload")
  private NamedVariableAccessor<String> payloadVariable;
  @SystemVariable(name = "contentType")
  private NamedVariableAccessor<String> contentTypeVariable;
  @SystemVariable(name = "response")
  private NamedVariableAccessor<SpinXmlElement> responseVariable;

  public TrembitaSoapConnectorDelegate(SoapHttpConnector soapHttpConnector,
      TrembitaExchangeGatewayProperties trembitaExchangeGatewayProperties) {
    this.soapHttpConnector = soapHttpConnector;
    this.trembitaExchangeGatewayProperties = trembitaExchangeGatewayProperties;
  }

  @Override
  protected void executeInternal(DelegateExecution execution) throws Exception {
    responseVariable.on(execution).set(Spin.XML(Map.of()));

    var trembitaSystemName = systemNameVariable.from(execution).getOrThrow();
    var soapAction = trembitaSoapActionVariable.from(execution).getOrThrow();
    var payload = payloadVariable.from(execution).getOrThrow();
    var contentType = contentTypeVariable.from(execution).getOrThrow();
    var registryProperties = getTrembitaRegistryProperties(trembitaSystemName);

    var payloadStream = new ByteArrayInputStream(payload.getBytes());
    var soapMessage = MessageFactory.newInstance().createMessage(null, payloadStream);

    log.debug("Start adding trembita system headers");
    addSystemTrembitaHeader(soapMessage, registryProperties, trembitaSystemName);
    log.debug("Trembita system headers added successfully");

    var payloadOutputStream = new ByteArrayOutputStream();
    soapMessage.writeTo(payloadOutputStream);
    var payloadString = payloadOutputStream.toString();

    log.debug("Start sending soap http request");
    var soapHttpResponse = soapHttpConnector.createRequest()
        .url(registryProperties.getUrl())
        .soapAction(soapAction)
        .contentType(contentType)
        .payload(payloadString)
        .execute();
    log.debug("Got response with status: {}", soapHttpResponse.getStatusCode());

    var xmlElementResponse = Spin.XML(soapHttpResponse.getResponse());
    responseVariable.on(execution).set(xmlElementResponse);
  }

  @Override
  public String getDelegateName() {
    return DELEGATE_NAME;
  }

  private RegistryProperties getTrembitaRegistryProperties(String systemName) {
    return Optional.ofNullable(trembitaExchangeGatewayProperties.getRegistries().get(systemName))
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Trembita system configuration with name %s not configured", systemName)));
  }

  private void addSystemTrembitaHeader(SOAPMessage soapMessage,
      RegistryProperties registryProperties, String trembitaSystemName) throws SOAPException {
    var envelope = soapMessage.getSOAPPart().getEnvelope();
    var header = soapMessage.getSOAPHeader();

    if (Objects.isNull(header)) {
      header = soapMessage.getSOAPPart().getEnvelope().addHeader();
    }

    addClientHeader(header, envelope, registryProperties, trembitaSystemName);
    addServiceHeader(header, envelope, registryProperties, trembitaSystemName);
    addUserIdHeader(header, envelope, registryProperties);
    addIdHeader(header, envelope);
    addProtocolVersionHeader(header, envelope, registryProperties);
  }

  private void addClientHeader(SOAPHeader soapHeader, SOAPEnvelope soapEnvelope,
      RegistryProperties registryProperties, String trembitaSystemName) throws SOAPException {
    var soapHeaderElement = addComplexHeaderElement(soapHeader, soapEnvelope,
        TREMBITA_PROPERTY_CLIENT, TREMBITA_PROPERTY_SUBSYSTEM);

    var client = registryProperties.getClient();
    checkTrembitaSubsystemProperties(client, trembitaSystemName);
    var xRoadInstance = client.getXRoadInstance();
    var memberClass = client.getMemberClass();
    var memberCode = client.getMemberCode();
    var subsystemCode = client.getSubsystemCode();
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_X_ROAD_INSTANCE, xRoadInstance);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_MEMBER_CLASS, memberClass);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_MEMBER_CODE, memberCode);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_SUBSYSTEM_CODE, subsystemCode);
  }

  private void addServiceHeader(SOAPHeader soapHeader, SOAPEnvelope soapEnvelope,
      RegistryProperties registryProperties, String trembitaSystemName) throws SOAPException {
    var soapHeaderElement = addComplexHeaderElement(soapHeader, soapEnvelope,
        TREMBITA_PROPERTY_SERVICE, TREMBITA_PROPERTY_SERVICE_CAPS);

    var service = registryProperties.getService();
    checkTrembitaSubsystemProperties(service, trembitaSystemName);
    var xRoadInstance = service.getXRoadInstance();
    var memberClass = service.getMemberClass();
    var memberCode = service.getMemberCode();
    var subsystemCode = service.getSubsystemCode();
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_X_ROAD_INSTANCE, xRoadInstance);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_MEMBER_CLASS, memberClass);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_MEMBER_CODE, memberCode);
    addChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_SUBSYSTEM_CODE, subsystemCode);
    var serviceCode = service.getServiceCode();
    var serviceVersion = service.getServiceVersion();
    addOptionalChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_SERVICE_CODE, serviceCode);
    addOptionalChildHeaderElement(soapHeaderElement, TREMBITA_PROPERTY_SERVICE_VERSION, serviceVersion);
  }

  private void checkTrembitaSubsystemProperties(TrembitaSubsystemProperties subsystemProperties,
      String systemName) {
    if (Objects.isNull(subsystemProperties)) {
      throw new IllegalArgumentException(
          String.format("Trembita system configuration with name %s not configured", systemName));
    }
  }

  private void addOptionalChildHeaderElement(SOAPElement soapHeaderElement, String childElementName,
      String value) throws SOAPException {
    if (StringUtils.isNotBlank(value)) {
      addChildHeaderElement(soapHeaderElement, childElementName, value);
    }
  }

  private void addIdHeader(SOAPHeader soapHeader, SOAPEnvelope envelope) throws SOAPException {
    var requestId = UUID.randomUUID().toString();
    addSimpleHeaderElement(soapHeader, envelope, TREMBITA_PROPERTY_ID, requestId);
  }

  private void addUserIdHeader(SOAPHeader soapHeader, SOAPEnvelope envelope,
      RegistryProperties registryProperties) throws SOAPException {
    var userId = registryProperties.getUserId();
    addSimpleHeaderElement(soapHeader, envelope, TREMBITA_PROPERTY_USER_ID, userId);
  }

  private void addProtocolVersionHeader(SOAPHeader soapHeader, SOAPEnvelope envelope,
      RegistryProperties registryProperties) throws SOAPException {
    var protocolVer = registryProperties.getProtocolVersion();
    addSimpleHeaderElement(soapHeader, envelope, TREMBITA_PROPERTY_PROTOCOL_VERSION, protocolVer);
  }

  private void addChildHeaderElement(SOAPElement soapHeaderElement, String childElementName,
      String value) throws SOAPException {
    var soapElement = soapHeaderElement.addChildElement(childElementName, TREMBITA_PREFIX_IDEN);
    soapElement.setValue(value);
  }

  private void addSimpleHeaderElement(SOAPHeader soapHeader, SOAPEnvelope envelope,
      String elementName, String value) throws SOAPException {
    var headerElement = envelope.createName(elementName, TREMBITA_PREFIX_XRO,
        TREMBITA_NAMESPACE_XROAD_XSD);
    var soapHeaderElement = soapHeader.addHeaderElement(headerElement);
    soapHeaderElement.setValue(value);
  }

  private SOAPHeaderElement addComplexHeaderElement(SOAPHeader soapHeader, SOAPEnvelope envelope,
      String elementName, String value) throws SOAPException {
    var headerElement = envelope.createName(elementName, TREMBITA_PREFIX_XRO,
        TREMBITA_NAMESPACE_XROAD_XSD);
    var qNameAttribute = new QName(TREMBITA_NAMESPACE_IDENTIFIERS, TREMBITA_PROPERTY_OBJECT_TYPE,
        TREMBITA_PREFIX_IDEN);
    var soapHeaderElement = soapHeader.addHeaderElement(headerElement);
    soapHeaderElement.addAttribute(qNameAttribute, value);
    return soapHeaderElement;
  }
}