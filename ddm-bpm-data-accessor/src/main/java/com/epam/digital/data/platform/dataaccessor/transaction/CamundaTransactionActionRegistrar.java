/*
 * Copyright 2025 EPAM Systems.
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

package com.epam.digital.data.platform.dataaccessor.transaction;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;

@RequiredArgsConstructor
public class CamundaTransactionActionRegistrar implements TransactionalActionRegistrar{

  public <T> void onCommitting(Consumer<T> consumer, T event) {
    Context.getCommandContext()
        .getTransactionContext()
        .addTransactionListener(
            TransactionState.COMMITTING, commandContext -> consumer.accept(event));
  }
}
