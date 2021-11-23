# ddm-bpm-engine

### Overview

* Module responsible for extending business-processes parsing / processing mechanisms and processing
  significant events.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-engine</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Add package `com.epam.digital.data.platform.bpms.engine` to scan.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-engine is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).