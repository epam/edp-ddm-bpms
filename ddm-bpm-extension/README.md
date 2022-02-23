# ddm-bpm-extension

### Overview

* Module responsible for system extensions, external integrations and integrations with IAM
  services.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-extension</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Add package `com.epam.digital.data.platform.bpms.extension` to scan.

3. Define these properties:

```yaml
spring:
  application:
    name: ddm-bpm-extension

registry-rest-api:
  url: data factory base url;

dso:
  url: digital signature ops service base url;

excerpt-service-api:
  url: base url for Excerpt connectors;

user-settings-service-api:
  url: base url for user settings connectors;

# for working with GetContentFromCephDelegate and PutContentToCephDelegate
ceph:
  http-endpoint: base ceph url
  access-key: access key for working with objects
  secret-key: secret key for working with objects
  bucket: bucket name for working with objects

keycloak:
  url: base keycloak url
  citizen:
    realm: citizen user realm name
    client-id: citizen user client identifier
    client-secret: citizen user client secret
  officer:
    realm: officer user realm name
    client-id: officer user client identifier
    client-secret: officer user client secret

# for working with delegates in com.epam.digital.data.platform.bpms.extension.delegate.storage package
storage:
  form-data:
    type: ceph # storage type
    backend: # contains specific properties for each storage type
      ceph:
        http-endpoint: http-endpoint
        access-key: access-key
        secret-key: secret-key
        bucket: bucket
  file-data:
    type: ceph
    backend:
      ceph:
        http-endpoint: http-endpoint
        access-key: access-key
        secret-key: secret-key
        bucket: bucket
```

### Available extensions

* `com.epam.digital.data.platform.bpms.extension.delegate.connector.keycloak` - contains connectors
  for working with keycloak client;
* `com.epam.digital.data.platform.bpms.extension.delegate.connector.registry` - contains connectors
  for working with EDR registry;
* `com.epam.digital.data.platform.bpms.extension.delegate.connector` - contains connectors for
  working with data factory;
* `com.epam.digital.data.platform.bpms.extension.delegate.storage` - contains connectors for working
  with storage;
* `com.epam.digital.data.platform.bpms.extension.delegate.CamundaSystemErrorDelegate` - used to
  throw a camunda system exception;
* `com.epam.digital.data.platform.bpms.extension.delegate.DefineBusinessProcessStatusDelegate` -
  used to define the status of a business process;
* `com.epam.digital.data.platform.bpms.extension.delegate.DefineProcessExcerptIdDelegate` - used to
  save excerpt id to a business process system variable;
* `com.epam.digital.data.platform.bpms.extension.delegate.UserDataValidationErrorDelegate` - used to
  throw a user data validation exception with details based on user input.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-extension is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).