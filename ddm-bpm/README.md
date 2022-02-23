# ddm-bpm

### Overview

* The main purpose of the ddm-bpm service is to extend the Camunda API.
* execution of business processes described in BPMN notation;
* execution of business rules described in DMN notation;
* execution of scripts in the scope of business process tasks.

### Usage

#### Prerequisites:

* Postgres database is configured and running;
* Ceph-storage is configured and running;
* Keycloak is configured and running;
* digital-signature-ops service is configured and running.

#### Configuration

Available properties are following:

```yaml
spring:
  datasource:
    driver-class-name: database driver class name;
    url: database url;
    username: user name;
    password: password;
    
registry-rest-api:
  url: data factory base url;
    
dso:
  url: digital signature ops service base url;
  
excerpt-service-api:
  url: base url for Excerpt connectors;

user-settings-service-api:
  url: base url for user settings connectors;
  
ceph:
  http-endpoint: base ceph url
  access-key: access key for working with objects
  secret-key: secret key for working with objects
  bucket:  bucket name for working with objects
  file-storage-access-key: access key for working with files
  file-storage-secret-key: secret key for working with files
  file-storage-bucket: bucket name for working with files

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
  system-user:
    realm: system user realm name
    client-id: system user client identifier
    client-secret: system user client secret
```

#### Run application:

* `java -jar <file-name>.jar`

### Local development

1. Run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
    * `application-local.yml` - configuration file for local profile.
2. The application will be available on: http://localhost:8080.

### Test execution

* Tests could be run via maven command:
    * `mvn verify -P local` OR using appropriate functions of your IDE.
    
### Postman collections

* `camunda.postman_collection.json` - this collection contain the rest-api enpoints related to
  Camunda REST API

#### To use collection:

- Import collection to your postman;
- Set up all required environments.

### License

The ddm-bpm is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).