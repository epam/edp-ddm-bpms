/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.it.config;

import com.google.common.net.HostAndPort;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.embedded.RedisCluster;

@Component
@ConditionalOnProperty(prefix = "storage.message-payload", name = "type", havingValue = "redis")
public class EmbeddedRedisConfig {

  private RedisCluster redisCluster;

  @Value("${storage.backend.redis.sentinel.master}")
  private String master;
  @Value("${storage.backend.redis.sentinel.nodes}")
  private String nodes;

  @PostConstruct
  public void postConstruct() {
      var hostAndPort = HostAndPort.fromString(nodes);
      redisCluster = RedisCluster.builder()
          .sentinelPorts(List.of(hostAndPort.getPort()))
          .sentinelCount(1)
          .quorumSize(1)
          .ephemeralServers()
          .replicationGroup(master, 1)
          .build();

      redisCluster.start();
  }

  @PreDestroy
  public void preDestroy() {
    redisCluster.stop();
  }
}
