package ua.gov.mdtu.ddm.lowcode.bpms.camunda.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResolver {

  private final MessageSource messageSource;

  public String getMessage(String msgCode) {
    return messageSource.getMessage(msgCode, null, LocaleContextHolder.getLocale());
  }
}
