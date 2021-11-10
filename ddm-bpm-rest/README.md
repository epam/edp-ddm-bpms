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

* `com.epam.digital.data.platform.bpms.rest.controller.HistoricTaskController` - for getting
  historical user tasks;
* `com.epam.digital.data.platform.bpms.rest.controller.StartFormController` - for getting
  process-definition start-forms;
* `com.epam.digital.data.platform.bpms.rest.controller.TaskController` - for getting user tasks;
* `com.epam.digital.data.platform.bpms.rest.controller.TaskPropertyController` - for getting
  extended task property.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-rest is released under version 2.0 of
the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).