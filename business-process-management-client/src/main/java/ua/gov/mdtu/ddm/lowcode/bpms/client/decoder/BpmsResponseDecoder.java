package ua.gov.mdtu.ddm.lowcode.bpms.client.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BpmsResponseDecoder implements Decoder {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Object decode(Response response, Type type) throws IOException {
    if (response.body() == null) {
      return null;
    }
    return objectMapper.readValue(
        Util.toByteArray(response.body().asInputStream()), objectMapper.constructType(type));
  }
}
