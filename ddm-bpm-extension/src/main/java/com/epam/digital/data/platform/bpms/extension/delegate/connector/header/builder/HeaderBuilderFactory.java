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

package com.epam.digital.data.platform.bpms.extension.delegate.connector.header.builder;

import com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable;
import com.epam.digital.data.platform.dataaccessor.named.NamedVariableAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class represents a factory that is used to create {@link HeaderBuilder} builder for headers.
 */
@Component
public class HeaderBuilderFactory {

  @SystemVariable(name = "x_access_token")
  private NamedVariableAccessor<String> xAccessTokenVariable;
  @SystemVariable(name = "x_digital_signature_ceph_key")
  private NamedVariableAccessor<String> xDigitalSignatureCephKeyVariable;
  @SystemVariable(name = "x_digital_signature_derived_ceph_key")
  private NamedVariableAccessor<String> xDigitalSignatureDerivedCephKeyVariable;

  @Value("${spring.application.name}")
  private String springAppName;

  public HeaderBuilder builder() {
    return new HeaderBuilder(xAccessTokenVariable, xDigitalSignatureCephKeyVariable,
        xDigitalSignatureDerivedCephKeyVariable, springAppName);
  }
}
