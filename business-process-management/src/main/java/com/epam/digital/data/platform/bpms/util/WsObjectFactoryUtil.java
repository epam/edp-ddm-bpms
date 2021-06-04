package com.epam.digital.data.platform.bpms.util;

import com.epam.digital.data.platform.bpms.generated.ws.registry.edr.ObjectFactory;

public class WsObjectFactoryUtil {

  private static final ObjectFactory edrRegistryObjectFactory = new ObjectFactory();

  public static ObjectFactory edrRegistry() {
    return edrRegistryObjectFactory;
  }

}
