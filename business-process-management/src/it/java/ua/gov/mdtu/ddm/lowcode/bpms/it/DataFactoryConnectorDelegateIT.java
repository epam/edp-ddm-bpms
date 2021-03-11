package ua.gov.mdtu.ddm.lowcode.bpms.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.CamundaSystemException;
import ua.gov.mdtu.ddm.lowcode.bpms.exception.UserDataValidationException;

public class DataFactoryConnectorDelegateIT extends BaseIT {

  private static final String CONTENT = "{\"x-access-token\":\"token\"}";

  @Inject
  @Qualifier("cephWireMockServer")
  private WireMockServer cephWireMockServer;
  @Inject
  @Qualifier("dataFactoryMockServer")
  private WireMockServer dataFactoryMockServer;
  @Inject
  @Qualifier("digitalSignatureMockServer")
  private WireMockServer digitalSignatureMockServer;
  @Value("${ceph.bucket}")
  private String cephBucketName;

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(200))));

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key");
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testNotFoundDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(404)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"NOT_FOUND\"}"))));

    var ex = assertThrows(UserDataValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex.getErrorDto()).isNotNull();
    assertThat(ex.getErrorDto().getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getErrorDto().getCode()).isEqualTo("NOT_FOUND");
    assertThat(ex.getErrorDto().getMessage()).isEqualTo("Validation error");
    assertThat(ex.getErrorDto().getDetails()).isNull();
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testValidationErrorDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"VALIDATION_ERROR\","
                    + "\"details\":{\"errors\":[{\"field\":\"field1\",\"value\":\"value1\","
                    + "\"message\":\"message1\"}]}}"))));

    var ex = assertThrows(UserDataValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex.getErrorDto()).isNotNull();
    assertThat(ex.getErrorDto().getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getErrorDto().getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(ex.getErrorDto().getMessage()).isEqualTo("Validation error");
    assertThat(ex.getErrorDto().getDetails()).isNotNull();
    assertThat(ex.getErrorDto().getDetails().getErrors()).hasSize(1);
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getMessage())
        .isEqualTo("message1");
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getField())
        .isEqualTo("field1");
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getValue())
        .isEqualTo("value1");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorReadDelegate.bpmn"})
  public void testServerErrorDataFactoryConnectorReadDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(409)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"CONSTRAINT_VIOLATION\"}"))));

    var ex = assertThrows(CamundaSystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorReadDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getType()).isEqualTo("CONSTRAINT_VIOLATION");
    assertThat(ex.getMessage()).isEqualTo("System Error");
    assertThat(ex.getLocalizedMessage()).isEqualTo("Порушення одного з обмежень на рівні БД");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorDeleteDelegate.bpmn"})
  public void testDataFactoryConnectorDeleteDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(delete(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(400)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"HEADERS_ARE_MISSING\"}"))));

    var ex = assertThrows(CamundaSystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorDeleteDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getType()).isEqualTo("HEADERS_ARE_MISSING");
    assertThat(ex.getMessage()).isEqualTo("System Error");
    assertThat(ex.getLocalizedMessage()).isEqualTo("Відсутній обов’язковий заголовок");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorUpdateDelegate.bpmn"})
  public void testDataFactoryConnectorUpdateDelegate() {
    cephWireMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/")).willReturn(
            aResponse()
                .withStatus(200)
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListAllMyBucketsResult>"
                    + "<Buckets><Bucket><Name>" + cephBucketName + "</Name></Bucket></Buckets>"
                    + "</ListAllMyBucketsResult>"))));

    cephService.putContent(cephBucketName, "cephKey", CONTENT);

    dataFactoryMockServer.addStubMapping(
        stubFor(put(urlPathEqualTo("/mock-server/laboratory/id"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withHeader("X-Digital-Signature", equalTo("cephKey"))
            .withHeader("X-Digital-Signature-Derived", equalTo("cephKey"))
            .withRequestBody(equalTo("{\"var\", \"value\"}"))
            .willReturn(aResponse().withStatus(422)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"VALIDATION_ERROR\","
                    + "\"details\":{\"errors\":[{\"field\":\"field1\",\"value\":\"value1\","
                    + "\"message\":\"message1\"}]}}"))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-testActivity", "cephKey");
    var ex = assertThrows(UserDataValidationException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorUpdateDelegate_key", variables));

    assertThat(ex.getErrorDto()).isNotNull();
    assertThat(ex.getErrorDto().getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getErrorDto().getCode()).isEqualTo("VALIDATION_ERROR");
    assertThat(ex.getErrorDto().getMessage()).isEqualTo("Validation error");
    assertThat(ex.getErrorDto().getDetails()).isNotNull();
    assertThat(ex.getErrorDto().getDetails().getErrors()).hasSize(1);
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getMessage())
        .isEqualTo("message1");
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getField())
        .isEqualTo("field1");
    assertThat(ex.getErrorDto().getDetails().getErrors().get(0).getValue())
        .isEqualTo("value1");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorSearchDelegate.bpmn"})
  public void testDataFactoryConnectorSearchDelegate() {
    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlPathEqualTo("/mock-server/laboratory"))
            .withQueryParam("id", equalTo("id1"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .withHeader("x-custom-header", equalTo("custom header value"))
            .willReturn(aResponse().withStatus(200))));

    dataFactoryMockServer.addStubMapping(
        stubFor(get(urlEqualTo("/mock-server/laboratory"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("X-Source-System", equalTo("Low-code Platform"))
            .withHeader("X-Source-Application", equalTo("business-process-management"))
            .willReturn(aResponse().withStatus(412)
                .withBody("{\"traceId\":\"traceId1\",\"code\":\"SIGNATURE_VIOLATION\"}"))));

    var ex = assertThrows(CamundaSystemException.class, () -> runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorSearchDelegate_key"));

    assertThat(ex.getTraceId()).isEqualTo("traceId1");
    assertThat(ex.getType()).isEqualTo("SIGNATURE_VIOLATION");
    assertThat(ex.getMessage()).isEqualTo("System Error");
    assertThat(ex.getLocalizedMessage()).isEqualTo("Данні в тілі не відповідають підпису");
  }

  @Test
  @Deployment(resources = {"bpmn/connector/testDataFactoryConnectorBatchCreateDelegate.bpmn"})
  public void testDataFactoryConnectorBatchCreateDelegate() {
    cephService.putContent(cephBucketName, "tokenKey", CONTENT);

    digitalSignatureMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo(
                "{\"data\":\"{\\\"data\\\":\\\"test data\\\",\\\"description\\\":\\\"some description\\\"}\"}"))
            .willReturn(aResponse().withStatus(200).withBody("{\"signature\":\"signature\"}"))));
    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/test"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withHeader("X-Digital-Signature", equalTo("testSignature"))
            .withHeader("X-Digital-Signature-Derived",
                matching("lowcode_.*_system_signature_ceph_key_0"))
            .withRequestBody(
                equalTo("{\"data\":\"test data\",\"description\":\"some description\"}"))
            .willReturn(aResponse().withStatus(201))));

    digitalSignatureMockServer.addStubMapping(stubFor(
        post(urlPathEqualTo("/api/eseal/sign"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withRequestBody(equalTo(
                "{\"data\":\"{\\\"data2\\\":\\\"test data2\\\",\\\"description2\\\":\\\"some description2\\\"}\"}"))
            .willReturn(aResponse().withStatus(200).withBody("{\"signature\":\"signature2\"}"))));
    dataFactoryMockServer.addStubMapping(
        stubFor(post(urlPathEqualTo("/mock-server/test"))
            .withHeader("X-Access-Token", equalTo("token"))
            .withHeader("X-Digital-Signature", equalTo("testSignature"))
            .withHeader("X-Digital-Signature-Derived",
                matching("lowcode_.*_system_signature_ceph_key_1"))
            .withRequestBody(
                equalTo("{\"data2\":\"test data2\",\"description2\":\"some description2\"}"))
            .willReturn(aResponse().withStatus(201))));

    Map<String, Object> variables = ImmutableMap
        .of("secure-sys-var-ref-task-form-data-test_token", "tokenKey",
            "secure-sys-var-ref-task-form-data-test_signature", "testSignature");

    var processInstance = runtimeService
        .startProcessInstanceByKey("testDataFactoryConnectorBatchCreateDelegate_key", variables);
    assertThat(processInstance.isEnded()).isTrue();
  }
}
