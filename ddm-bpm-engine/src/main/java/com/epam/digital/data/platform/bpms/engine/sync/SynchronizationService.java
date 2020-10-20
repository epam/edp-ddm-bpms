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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Service that is used for synchronization by business keys.
 */
@Component
public class SynchronizationService {

  private final Cache<Object, ReentrantLock> lockCache;

  public SynchronizationService() {
    lockCache = CacheBuilder.newBuilder().weakKeys().build();
  }

  /**
   * Returns or creates lock that is bound to business key for self-use.
   *
   * @param key business key that has to be synchronized
   * @return lock that is bound to the key
   */
  public ReentrantLock getLock(Object key) {
    return lockCache.asMap().computeIfAbsent(key, obj -> new ReentrantLock());
  }

  /**
   * Waits for lock that is bound to business key, acquires it, executes runnable process and
   * releases the lock.
   *
   * @param key      business key
   * @param runnable process that has to be synchronized by the key
   */
  public void execute(Object key, Runnable runnable) {
    var lock = getLock(key);
    lock.lock();
    executeAndUnlock(runnable, lock);
  }

  /**
   * Waits for lock that is bound to business key, acquires it, executes supplier process and
   * releases the lock.
   *
   * @param key      business key
   * @param supplier supplier that has to be synchronized by the key
   * @return supplier result
   */
  public <R> R evaluate(Object key, Supplier<R> supplier) {
    var lock = getLock(key);
    lock.lock();
    return evaluateAndUnlock(supplier, lock);
  }

  /**
   * Tries to lock the lock that is bound to business key. If it's already locked then throws
   * exception that is gotten from {@code exceptionSupplier} else executes runnable process and
   * releases the lock.
   *
   * @param key               business key
   * @param runnable          process that has to be synchronized by the key
   * @param exceptionSupplier supplier that returns the exception that has to be thrown if the key
   *                          is already locked
   */
  public <T extends Throwable> void executeOrThrow(Object key, Runnable runnable,
      Supplier<T> exceptionSupplier) throws T {
    var lock = getLock(key);
    tryLockOrThrow(lock, exceptionSupplier);
    executeAndUnlock(runnable, lock);
  }

  /**
   * Tries to lock the lock that is bound to business key, if it's already locked then throws
   * exception that is gotten from {@code exceptionSupplier} else executes supplier process and
   * releases the lock.
   *
   * @param key               business key
   * @param supplier          supplier that has to be synchronized by the key
   * @param exceptionSupplier supplier that returns the exception that has to be thrown if the key
   *                          is already locked
   * @return supplier result
   */
  public <T extends Throwable, R> R evaluateOrThrow(Object key, Supplier<R> supplier,
      Supplier<T> exceptionSupplier) throws T {
    var lock = getLock(key);
    tryLockOrThrow(lock, exceptionSupplier);
    return evaluateAndUnlock(supplier, lock);
  }

  private void executeAndUnlock(Runnable runnable, ReentrantLock lock) {
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  private <R> R evaluateAndUnlock(Supplier<R> supplier, ReentrantLock lock) {
    try {
      return supplier.get();
    } finally {
      lock.unlock();
    }
  }

  private <T extends Throwable> void tryLockOrThrow(ReentrantLock lock,
      Supplier<T> exceptionSupplier) throws T {
    if (!lock.tryLock()) {
      throw exceptionSupplier.get();
    }
  }

}
