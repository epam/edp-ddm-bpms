package com.epam.digital.data.platform.bpms.engine.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.epam.digital.data.platform.bpms.engine")
public class TestDelegateApp {

  public static void main(String[] args) {
    SpringApplication.run(TestDelegateApp.class, args);
  }
}
