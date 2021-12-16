package com.epam.digital.data.platform.bpm.history.kafka.exception;

public class CreateKafkaTopicException extends RuntimeException {

  public CreateKafkaTopicException(String message, Exception e) {
    super(message, e);
  }
}
