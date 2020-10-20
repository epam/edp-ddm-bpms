# ddm-bpm-storage

### Overview

* Module responsible for managing form and file data in the storage during process execution.

### Usage

1. Specify dependency in your service:

```xml

<dependencies>
  ...
  <dependency>
    <groupId>com.epam.digital.data.platform</groupId>
    <artifactId>ddm-bpm-storage</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

2. Add package `com.epam.digital.data.platform.bpms.storage` to scan.

3. Define these properties:

```yaml
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

### Available storage types

* `ceph` - uses Ceph storage for getting access to form and file data

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The ddm-bpm-extension is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).