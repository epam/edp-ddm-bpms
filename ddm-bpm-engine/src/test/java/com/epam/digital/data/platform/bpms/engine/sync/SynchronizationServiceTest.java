/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.bpms.engine.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SynchronizationServiceTest {

  private final SynchronizationService service = new SynchronizationService();
  private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

  @Test
  void testConsistentOperations() throws ExecutionException, InterruptedException {
    var list = new ArrayList<Integer>();

    threadPool.execute(() -> service.execute(1, () -> {
      sleep(300);
      list.add(1);
    }));
    sleep(100);
    var result = threadPool.submit(() -> service.evaluate(1, () -> {
      list.add(2);
      return 2;
    })).get();

    assertThat(result).isEqualTo(2);
    assertThat(list.get(0)).isEqualTo(1);
    assertThat(list.get(1)).isEqualTo(2);
  }

  @Test
  void testThrowsIfLocked() throws ExecutionException, InterruptedException {
    var list = new ArrayList<Integer>();

    var future1 = threadPool.submit(() -> service.executeOrThrow(1, () -> {
      sleep(300);
      list.add(1);
    }, RuntimeException::new));
    sleep(100);
    var future2 = threadPool.submit(
        () -> service.evaluateOrThrow(1, () -> "result", () -> new RuntimeException("error")));
    var exception = assertThrows(ExecutionException.class, future2::get);

    future1.get();

    assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
    assertThat(exception.getCause().getMessage()).isEqualTo("error");
    assertThat(list).hasSize(1).contains(1);
  }

  @SneakyThrows
  private void sleep(int millis) {
    Thread.sleep(millis);
  }
}
