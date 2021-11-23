# ddm-bpm-data-accessor

### Overview

* Module responsible for working with context variables using Data Accessors.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-data-accessor</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Auto-configuration should be activated through the `@SpringBootApplication` annotation or
   using `@EnableAutoConfiguration` annotation in main class

3. Inject `com.epam.digital.data.platform.dataaccessor.VariableAccessorFactory` for
   creating [base variable accessor](src/main/java/com/epam/digital/data/platform/dataaccessor/VariableAccessor.java);

4. Define Spring bean field with
   type [NamedVariableAccessor](src/main/java/com/epam/digital/data/platform/dataaccessor/named/NamedVariableAccessor.java)
   annotated by `com.epam.digital.data.platform.dataaccessor.annotation.SystemVariable` for creating
   variable accessor with predefined name and type.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-data-accessor is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).