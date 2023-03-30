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

package com.epam.digital.data.platform.bpms.extension.service;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Service that is used for storing and accessing token in cache for its reusing.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TokenCacheService {

  private final CacheManager cacheManager;


  /**
   * Checks if there are a token in cache with name {@code cacheName} and key {@code serviceName}.
   * If there is stored cache for this service in this cache then tries to parse it and get an
   * expiration time from the cache. If an expiration time is after than current datetime + 5
   * seconds (making sure that the token won't be expired in next 5 seconds) then method returns
   * cached token. Else if token couldn't be parsed (it's not a JWT) or token is expired then use
   * {@code tokenSupplier} to get a new token, store it in cache instead of existing one and return
   * it.
   * <p>
   * Nullable because it's possible for the {@code tokenSupplier} to return null.
   *
   * @param cacheName     name of the cache to find token in (token type is normally used)
   * @param serviceName   name of the service where stored token is used for an authentication (used
   *                      as token cache key)
   * @param tokenSupplier supplier of a token that is used if stored token not found or is expired
   * @return an unexpired token for an input service
   */
  @Nullable
  public String getCachedTokenOrElse(@NonNull String cacheName, @NonNull String serviceName,
      @NonNull Supplier<String> tokenSupplier) {
    log.debug("Getting token for service {} from cache {}", serviceName, cacheName);
    final var cache = Objects.requireNonNull(cacheManager.getCache(cacheName));
    final var cachedToken = cache.get(serviceName, String.class);

    var expirationTime = getExpirationTimeFromToken(cachedToken);
    if (Objects.nonNull(expirationTime) && expirationTime.after(getCurrentDatePlusFiveSeconds())) {
      return cachedToken;
    }

    var token = tokenSupplier.get();
    if (Objects.nonNull(getExpirationTimeFromToken(token))) {
      cache.put(serviceName, token);
    }
    return token;
  }

  /**
   * Parses input token and get an expiration time from it. If an input token is {@code null} or
   * cannot be parsed (it's not a JWT) then returns {@code null} as an expiration time.
   *
   * @param token nullable token to parse and get expiration time
   * @return found expiration time or {@code null} if it's not found for any reason
   */
  @Nullable
  private static Date getExpirationTimeFromToken(@Nullable String token) {
    if (Objects.isNull(token)) {
      return null;
    }
    try {
      return SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
    } catch (ParseException e) {
      log.warn("Couldn't parse JWT token: {}", e.getMessage());
      return null;
    }
  }

  @VisibleForTesting
  Date getCurrentDatePlusFiveSeconds() {
    return Date.from(Instant.now().plusSeconds(5));
  }
}
