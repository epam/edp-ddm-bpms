package ua.gov.mdtu.ddm.lowcode.bpms.client.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import feign.error.AnnotationErrorDecoder;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.gov.mdtu.ddm.lowcode.bpms.client.BaseFeignClient;

@Component
public class BpmsAnnotationErrorDecoder implements ErrorDecoder {

  @Autowired
  private List<BaseFeignClient> baseFeignClients;
  @Autowired
  private BpmsResponseDecoder bpmsResponseDecoder;

  private ErrorDecoder errorDecoderChain;

  @PostConstruct
  public void init() {
    errorDecoderChain = new Default();

    for (BaseFeignClient baseFeignClient : baseFeignClients) {
      Class<?> feignClientType = baseFeignClient.getClass().getInterfaces()[0];

      errorDecoderChain = AnnotationErrorDecoder.builderFor(feignClientType)
          .withDefaultDecoder(errorDecoderChain)
          .withResponseBodyDecoder(bpmsResponseDecoder).build();
    }
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    return errorDecoderChain.decode(methodKey, response);
  }
}
