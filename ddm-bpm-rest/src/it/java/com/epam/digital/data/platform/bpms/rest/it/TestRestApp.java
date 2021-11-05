package com.epam.digital.data.platform.bpms.rest.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.epam.digital.data.platform.bpms.rest",
    "com.epam.digital.data.platform.bpms.security", "com.epam.digital.data.platform.bpms.engine"})
public class TestRestApp {

  public static void main(String[] args) {
    SpringApplication.run(TestRestApp.class, args);
  }
}
