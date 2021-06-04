package com.epam.digital.data.platform.bpms.config;

import com.epam.digital.data.platform.bpms.client.EdrRegistryClient;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadClientIdentifierType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadObjectType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadServiceIdentifierType;
import com.epam.digital.data.platform.bpms.mapper.TrembitaSubsystemPropertiesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * The class contains beans of the registry clients configuration.
 */
@Slf4j
@Configuration
public class RegistryClientsConfig {

  /**
   * The bean describes platform subsystem which has access to registries. Each registry client uses
   * it during communication through Trembita.
   */
  @Bean
  @ConditionalOnProperty(value = "trembita-exchange-gateway.client.x-road-instance")
  public XRoadClientIdentifierType clientSubsystem(
      TrembitaExchangeGatewayProperties trembitaExchangeGatewayProperties) {

    XRoadClientIdentifierType clientSubsystem = TrembitaSubsystemPropertiesMapper.INSTANCE
        .toXRoadClientIdentifier(trembitaExchangeGatewayProperties.getClient());
    clientSubsystem.setObjectType(XRoadObjectType.SUBSYSTEM);
    return clientSubsystem;
  }

  @Bean
  @ConditionalOnBean(XRoadClientIdentifierType.class)
  @ConditionalOnProperty(value = "trembita-exchange-gateway.registries.edr-registry.x-road-instance")
  public EdrRegistryClient edrRegistryClient(
      XRoadClientIdentifierType clientSubsystem,
      TrembitaExchangeGatewayProperties trembitaExchangeGatewayProperties) {

    TrembitaSubsystemProperties registryProperties = trembitaExchangeGatewayProperties
        .getRegistries().get("edr-registry");
    XRoadServiceIdentifierType xRoadServiceIdentifierType = TrembitaSubsystemPropertiesMapper.INSTANCE
        .toXRoadServiceIdentifier(registryProperties);
    xRoadServiceIdentifierType.setObjectType(XRoadObjectType.SERVICE);

    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setContextPath("com.epam.digital.data.platform.bpms.generated.ws.registry.edr");

    EdrRegistryClient client = new EdrRegistryClient(clientSubsystem, xRoadServiceIdentifierType,
        trembitaExchangeGatewayProperties);
    client.setDefaultUri(trembitaExchangeGatewayProperties.getUrl());
    client.setUnmarshaller(marshaller);
    client.setMarshaller(marshaller);
    return client;
  }

}
