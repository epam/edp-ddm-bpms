# ddm-bpm-security

### Overview

* Module responsible for managing camunda authentication and authorization for user.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-security</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Add package `com.epam.digital.data.platform.bpms.security` to scan.

3. Define these properties:

```yaml
camunda:
  admin-user-id: admin user identifier
  admin-group-id: admin group identifier
```

### Available functionality

* `com.epam.digital.data.platform.bpms.security.CamundaAuthorizationFilter` - a filter that sets
  camunda authorizations for authenticated user;
* `com.epam.digital.data.platform.bpms.security.CamundaImpersonation` - camunda user impersonation;
* `com.epam.digital.data.platform.bpms.security.listener.AuthorizationStartEventListener` - set
  authorizations for current user before process instance starts;
* `com.epam.digital.data.platform.bpms.security.listener.CompleterTaskEventListener` - set completer
  token and username after task completion.
* `com.epam.digital.data.platform.bpms.security.listener.InitiatorTokenStartEventListener` - set
  authorizations for current user before process instance starts.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-security is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).