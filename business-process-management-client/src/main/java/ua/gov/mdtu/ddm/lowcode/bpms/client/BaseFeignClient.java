package ua.gov.mdtu.ddm.lowcode.bpms.client;

import feign.error.ErrorCodes;
import feign.error.ErrorHandling;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.AuthorizationException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.BadRequestException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.CamundaCommunicationException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.InternalServerErrorException;
import ua.gov.mdtu.ddm.lowcode.bpms.client.exception.NotFoundException;

@ErrorHandling(codeSpecific = {
    @ErrorCodes(codes = {400}, generate = BadRequestException.class),
    @ErrorCodes(codes = {403}, generate = AuthorizationException.class),
    @ErrorCodes(codes = {404}, generate = NotFoundException.class),
    @ErrorCodes(codes = {500}, generate = InternalServerErrorException.class),
    @ErrorCodes(codes = {502, 503, 504}, generate = CamundaCommunicationException.class)
})
public interface BaseFeignClient {

}
