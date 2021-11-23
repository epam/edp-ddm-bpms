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

package com.epam.digital.data.platform.bpms.client.exception;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import feign.error.FeignExceptionConstructor;

/**
 * The class represents an exception which will be thrown in case when a process definition was not
 * found.
 */
public class ProcessDefinitionNotFoundException extends NotFoundException {

  @FeignExceptionConstructor
  public ProcessDefinitionNotFoundException(SystemErrorDto errorDto) {
    super(errorDto);
  }
}
