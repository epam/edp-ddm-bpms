# ddm-bpm-client

### Overview

* Module responsible for executing Camunda requests using a feign client.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-client</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Import `com.epam.digital.data.platform.bpms.client.config.FeignConfig` configuration to your
   service;
3. Inject the appropriate child of `com.epam.digital.data.platform.bpms.client.BaseFeignClient`.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-client is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).