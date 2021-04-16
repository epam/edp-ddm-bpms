package com.epam.digital.data.platform.bpms.client;

import com.epam.digital.data.platform.bpms.client.exception.AuthenticationException;
import com.epam.digital.data.platform.bpms.client.exception.AuthorizationException;
import com.epam.digital.data.platform.bpms.client.exception.BadRequestException;
import com.epam.digital.data.platform.bpms.client.exception.CamundaCommunicationException;
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
    @ErrorCodes(codes = {500}, generate = InternalServerErrorException.class),
    @ErrorCodes(codes = {502, 503, 504}, generate = CamundaCommunicationException.class)
})
public interface BaseFeignClient {

}
