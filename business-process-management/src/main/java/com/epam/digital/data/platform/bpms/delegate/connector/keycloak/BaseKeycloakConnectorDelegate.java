package com.epam.digital.data.platform.bpms.delegate.connector.keycloak;

import com.epam.digital.data.platform.bpms.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.bpms.exception.KeycloakException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

@Slf4j
public abstract class BaseKeycloakConnectorDelegate extends BaseJavaDelegate {

  protected abstract String realmName();

  protected abstract Keycloak keycloakClient();

  protected RealmResource realmResource() {
    var realmName = realmName();
    log.info("Selecting keycloak realm {}", realmName);
    var result = wrapKeycloakRequest(() -> keycloakClient().realm(realmName),
        () -> String.format("Couldn't find realm %s", realmName));
    log.info("Keycloak realm {} found", realmName);
    return result;
  }

  protected <T> T wrapKeycloakRequest(Supplier<T> supplier, Supplier<String> failMessageSupplier) {
    try {
      return supplier.get();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }

  protected void wrapKeycloakVoidRequest(Runnable runnable, Supplier<String> failMessageSupplier) {
    try {
      runnable.run();
    } catch (RuntimeException exception) {
      throw new KeycloakException(failMessageSupplier.get(), exception);
    }
  }
}
