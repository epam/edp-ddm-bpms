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
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import lombok.RequiredArgsConstructor;
import org.camunda.spin.json.SpinJsonNode;

/**
 * The class used to access {@link FormDataDto} entity in storage using {@link FormDataStorageService}
 */
@RequiredArgsConstructor
public abstract class BaseFormDataDelegate extends BaseJavaDelegate {

  protected final FormDataStorageService formDataStorageService;

  @SystemVariable(name = "taskDefinitionKey")
  protected NamedVariableAccessor<String> taskDefinitionKeyVariable;
  @SystemVariable(name = "formData", isTransient = true)
  protected NamedVariableAccessor<SpinJsonNode> formDataVariable;
}
