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

package com.epam.digital.data.platform.bpms.extension.delegate.storage;

import com.epam.digital.data.platform.bpms.extension.delegate.BaseJavaDelegate;
import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import lombok.RequiredArgsConstructor;

/**
 * Base class that is used for accessing ceph data as string using {@link CephService} service.
 */
@RequiredArgsConstructor
public abstract class BaseCephDelegate extends BaseJavaDelegate {

  protected final String cephBucketName;
  protected final CephService cephService;

  @SystemVariable(name = "key")
  protected NamedVariableAccessor<String> keyVariable;
  @SystemVariable(name = "content", isTransient = true)
  protected NamedVariableAccessor<String> contentVariable;
}
