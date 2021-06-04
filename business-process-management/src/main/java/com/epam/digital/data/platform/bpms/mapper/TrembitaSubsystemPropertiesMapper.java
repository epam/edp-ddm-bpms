package com.epam.digital.data.platform.bpms.mapper;

import com.epam.digital.data.platform.bpms.config.TrembitaSubsystemProperties;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadClientIdentifierType;
import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.XRoadServiceIdentifierType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrembitaSubsystemPropertiesMapper {

  TrembitaSubsystemPropertiesMapper INSTANCE = Mappers.getMapper(TrembitaSubsystemPropertiesMapper.class);

  XRoadClientIdentifierType toXRoadClientIdentifier(
      TrembitaSubsystemProperties trembitaSubsystemProperties);

  XRoadServiceIdentifierType toXRoadServiceIdentifier(
      TrembitaSubsystemProperties trembitaSubsystemProperties);

  XRoadServiceIdentifierType copy(XRoadServiceIdentifierType serviceIdentifierType);
}
