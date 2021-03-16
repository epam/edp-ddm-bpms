package ua.gov.mdtu.ddm.lowcode.bpms.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguraiton {

  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  @PostConstruct
  public void init() {
    JacksonConfigurator.setDateFormatString(DATE_TIME_FORMAT);
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> builder.simpleDateFormat(DATE_TIME_FORMAT)
        .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
  }
}
