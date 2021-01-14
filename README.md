##### Install platform-logger-spring-boot-starter library
##### For local development set active profile 'local'
##### Logger provide stdout in JSON format following predefined layout:

```javascript
{
  "@timestamp": {
    "$resolver": "timestamp",
    "pattern": {
      "format": "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      "timeZone": "UTC"
    }
  },
  "X-B3-TraceId": {
    "$resolver": "mdc",
    "key": "X-B3-TraceId",
    "stringified": true
  },
  "X-B3-SpanId": {
    "$resolver": "mdc",
    "key": "X-B3-SpanId",
    "stringified": true
  },
  "X-Request-Id": {
    "$resolver": "mdc",
    "key": "x-request-id",
    "stringified": true
  },
  "thread": {
    "$resolver": "thread",
    "field": "name"
  },
  "level": {
    "$resolver": "level",
    "field": "name"
  },
  "class": {
    "$resolver": "source",
    "field": "className"
  },
  "line_number": {
    "$resolver": "source",
    "field": "lineNumber"
  },
  "method": {
    "$resolver": "source",
    "field": "methodName"
  },
  "message": {
    "$resolver": "message",
    "stringified": true
  },
  "exception": {
    "type": {
      "$resolver": "exception",
      "field": "className"
    },
    "message": {
      "$resolver": "exception",
      "field": "message",
      "stringified": true
    },
    "stacktrace": {
      "$resolver": "exception",
      "field": "stackTrace",
      "stringified": true
    }
  }
}
```

##### Spring Actuator configured with Micrometer extension for exporting data in prometheus-compatible format.
*End-point:* <service>:<port>/actuator/prometheus

*Prometheus configuration example (prometheus.yml):*

```
global:
  scrape_interval: 10s
scrape_configs:
  - job_name: 'spring_micrometer'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['< service >:< port >']
```

##### Spring Sleuth configured for Istio http headers propagation:
- x-request-id
- x-b3-traceid
- x-b3-spanid
- x-b3-parentspanid
- x-b3-sampled
- x-b3-flags
- b3

### Local development  
1. create `low-code/mock-server` image:
    * download [low-code/mock-server](https://gitbud.epam.com/mdtu-ddm/low-code-platform/mock/data-factory-mock-server) project
    * open `data-factory-mock-server` project folder then create a docker image: `mvn package -P docker`
2. run docker compose: `docker-compose -f docker-compose-local.yml up`
3. run spring boot application using dev profile
    * `mvn spring-boot:run -Drun.profiles=dev` OR using appropriate functions of your IDE.
