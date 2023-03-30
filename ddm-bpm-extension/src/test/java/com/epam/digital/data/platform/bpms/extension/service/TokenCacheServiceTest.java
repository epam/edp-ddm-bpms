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

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("TokenCacheService#getCachedTokenOrElse")
class TokenCacheServiceTest {

  private static final String CACHE_NAME = "TOKEN_CACHE";
  private static final String SERVICE_NAME = "external-system-1";

  @Mock
  CacheManager cacheManager;
  @Mock
  Cache cache;

  @Spy
  @InjectMocks
  TokenCacheService tokenCacheService;

  @BeforeEach
  void setUp() {
    Mockito.doReturn(cache).when(cacheManager).getCache(CACHE_NAME);
  }

  @DisplayName("should return cached token if its expired")
  @Test
  void testCachedTokenIsNotExpired() {
    var expectedJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 12, 41, 29));
    Mockito.doReturn(expectedJWT).when(cache).get(SERVICE_NAME, String.class);
    Mockito.doReturn(Date.from(LocalDateTime.of(2023, 3, 30, 12, 41, 23).toInstant(ZoneOffset.UTC)))
        .when(tokenCacheService).getCurrentDatePlusFiveSeconds();

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME, () -> {
      throw new AssertionError("Supplier must not be executed");
    });

    Assertions.assertThat(actualJWT).isSameAs(expectedJWT);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should return token from supplier and store it in cache if cached token is expired")
  @Test
  void testCachedTokenIsExpired() {
    var expiredJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 12, 1));
    var expectedJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 16, 5));
    Mockito.doReturn(expiredJWT).when(cache).get(SERVICE_NAME, String.class);
    Mockito.doReturn(Date.from(LocalDateTime.of(2023, 3, 30, 14, 13, 55).toInstant(ZoneOffset.UTC)))
        .when(tokenCacheService).getCurrentDatePlusFiveSeconds();

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME,
        () -> expectedJWT);
    Assertions.assertThat(actualJWT).isSameAs(expectedJWT);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verify(cache).put(SERVICE_NAME, expectedJWT);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should return token from supplier and store it in cache if there are no tokens in cache")
  @Test
  void testNoCachedToken() {
    var expectedJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 16, 5));
    Mockito.doReturn(null).when(cache).get(SERVICE_NAME, String.class);
    Mockito.doReturn(Date.from(LocalDateTime.of(2023, 3, 30, 14, 13, 55).toInstant(ZoneOffset.UTC)))
        .when(tokenCacheService).getCurrentDatePlusFiveSeconds();

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME,
        () -> expectedJWT);
    Assertions.assertThat(actualJWT).isSameAs(expectedJWT);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verify(cache).put(SERVICE_NAME, expectedJWT);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should return token from supplier and store it in cache if there are no tokens in cache")
  @Test
  void testCachedTokenImpossibleToParse() {
    var expectedJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 16, 5));
    Mockito.doReturn("some unparsable token").when(cache).get(SERVICE_NAME, String.class);
    Mockito.doReturn(Date.from(LocalDateTime.of(2023, 3, 30, 14, 13, 55).toInstant(ZoneOffset.UTC)))
        .when(tokenCacheService).getCurrentDatePlusFiveSeconds();

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME,
        () -> expectedJWT);
    Assertions.assertThat(actualJWT).isSameAs(expectedJWT);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verify(cache).put(SERVICE_NAME, expectedJWT);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should return token from supplier and store it in cache even if token from supplier is expired too")
  @Test
  void testCachedTokenIsExpiredAndNewTokenTooExpired() {
    var expiredJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 12, 1));
    var expectedJWT = generateJWT(LocalDateTime.of(2023, 3, 30, 14, 12, 5));
    Mockito.doReturn(expiredJWT).when(cache).get(SERVICE_NAME, String.class);
    Mockito.doReturn(Date.from(LocalDateTime.of(2023, 3, 30, 14, 22, 50).toInstant(ZoneOffset.UTC)))
        .when(tokenCacheService).getCurrentDatePlusFiveSeconds();

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME,
        () -> expectedJWT);
    Assertions.assertThat(actualJWT).isSameAs(expectedJWT);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verify(cache).put(SERVICE_NAME, expectedJWT);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should throw NPE if no cache was found")
  @Test
  void testNoCacheFound() {
    Mockito.doReturn(null).when(cacheManager).getCache(CACHE_NAME);

    Assertions.assertThatThrownBy(
            () -> tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME, () -> null))
        .isInstanceOf(NullPointerException.class);

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("should return token from supplier but not store it in cache if it's unparsable")
  @Test
  void testNewTokenImpossibleToParse() {

    var actualJWT = tokenCacheService.getCachedTokenOrElse(CACHE_NAME, SERVICE_NAME,
        () -> "some unparsable token");
    Assertions.assertThat(actualJWT).isEqualTo("some unparsable token");

    Mockito.verify(cacheManager).getCache(CACHE_NAME);
    Mockito.verify(cache).get(SERVICE_NAME, String.class);
    Mockito.verifyNoMoreInteractions(cache, cacheManager);
  }

  @DisplayName("getCurrentDatePlusFiveSeconds should return date close to current")
  @Test
  void getCurrentDatePlusFiveSeconds() {
    Mockito.doCallRealMethod().when(tokenCacheService).getCurrentDatePlusFiveSeconds();
    var expectedDate = Date.from(Instant.now().plusSeconds(5));

    var actualDate = tokenCacheService.getCurrentDatePlusFiveSeconds();

    Assertions.assertThat(actualDate).isCloseTo(expectedDate, 500);
  }

  @SneakyThrows
  private String generateJWT(LocalDateTime expirationDateTime) {
    var key = new ECKeyGenerator(Curve.SECP256K1)
        .keyID(UUID.randomUUID().toString())
        .generate();

    var header = new JWSHeader.Builder(JWSAlgorithm.ES256K)
        .type(JOSEObjectType.JWT)
        .keyID(key.getKeyID())
        .build();
    var payload = new JWTClaimsSet.Builder()
        .issuer("http://keycloak:8080")
        .subject("admin_user")
        .expirationTime(Date.from(expirationDateTime.toInstant(ZoneOffset.UTC)))
        .build();

    var signedJWT = new SignedJWT(header, payload);
    signedJWT.sign(new ECDSASigner(key.toECPrivateKey()));
    return signedJWT.serialize();
  }
}
