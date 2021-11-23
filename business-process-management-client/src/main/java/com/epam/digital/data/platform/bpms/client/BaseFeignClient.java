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

package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.client.exception.AuthenticationException;
import com.epam.digital.data.platform.bpms.client.exception.AuthorizationException;
import com.epam.digital.data.platform.bpms.client.exception.BadRequestException;
import com.epam.digital.data.platform.bpms.client.exception.CamundaCommunicationException;
import com.epam.digital.data.platform.bpms.client.exception.ConflictException;
import com.epam.digital.data.platform.bpms.client.exception.InternalServerErrorException;
import com.epam.digital.data.platform.bpms.client.exception.NotFoundException;
import feign.error.ErrorCodes;
import feign.error.ErrorHandling;

/**
 * The interface represents a base feign client and contains common error handling logic for all
 * feign clients
 */
@ErrorHandling(codeSpecific = {
    @ErrorCodes(codes = {400}, generate = BadRequestException.class),
    @ErrorCodes(codes = {401}, generate = AuthenticationException.class),
    @ErrorCodes(codes = {403}, generate = AuthorizationException.class),
    @ErrorCodes(codes = {404}, generate = NotFoundException.class),
    @ErrorCodes(codes = {409}, generate = ConflictException.class),
    @ErrorCodes(codes = {500}, generate = InternalServerErrorException.class),
    @ErrorCodes(codes = {502, 503, 504}, generate = CamundaCommunicationException.class)
})
public interface BaseFeignClient {

}
