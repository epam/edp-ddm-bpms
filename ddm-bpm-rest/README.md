# ddm-bpm-rest

### Overview

* Module responsible for the Camunda REST API domain extension and error handling.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-rest</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Add package `com.epam.digital.data.platform.bpms.rest` to scan.

### Available controllers

* `com.epam.digital.data.platform.bpms.rest.controller.ProcessDefinitionController` - for accessing
  extended process definitions;
* `com.epam.digital.data.platform.bpms.rest.controller.ProcessInstanceController` - for accessing
  extended process instances;
* `com.epam.digital.data.platform.bpms.rest.controller.TaskController` - for getting extended user
  tasks;

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-rest is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).