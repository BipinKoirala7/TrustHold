package com.trusthold.shared.integration;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.shared.dtos.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.aop.proxy-target-class=false"
)
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestRestTemplate
public abstract class IntegrationTestSupport {

  protected LoginUserDto validLoginUserDto;
  protected RegisterUserDto validRegisterUserDto;

  @Autowired
  protected TestRestTemplate restTemplate;

  protected HttpHeaders getHeaders(){
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  protected HttpHeaders getAuthenticatedHeaders(){
    ResponseEntity<?> responseEntity = login(validLoginUserDto);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<String> setCookies = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
    assertThat(setCookies).as("login response should set cookies").isNotNull().isNotEmpty();

    String cookieHeader = setCookies.stream()
        .map(cookie -> cookie.split(";", 2)[0])
        .collect(Collectors.joining("; "));

    HttpHeaders headers = getHeaders();
    headers.set(HttpHeaders.COOKIE, cookieHeader);

    return headers;
  }

  private <T> HttpEntity<T> getHttpEntity(T body, HttpHeaders headers){
    return new HttpEntity<>(body, headers);
  }

  protected <T> ResponseEntity<ApiResponse<T>> get(String endpoint, HttpHeaders headers, Class<T> dataType) {
    ParameterizedTypeReference<ApiResponse<T>> type = ParameterizedTypeReference.forType(
        ResolvableType.forClassWithGenerics(ApiResponse.class, dataType).getType());
    return restTemplate.exchange(endpoint, HttpMethod.GET, getHttpEntity(null, headers), type);
  }

  protected <T> ResponseEntity<ApiResponse<Void>> post(String endpoint, HttpHeaders headers, T body) {
    return restTemplate.exchange(endpoint, HttpMethod.POST, getHttpEntity(body, headers), new ParameterizedTypeReference<>() {});
  }

  protected <T> ResponseEntity<ApiResponse<Void>> put(String endpoint, HttpHeaders headers, T body) {
    return restTemplate.exchange(endpoint, HttpMethod.PUT, getHttpEntity(body, headers), new ParameterizedTypeReference<>() {});
  }

  protected ResponseEntity<ApiResponse<Void>> register(RegisterUserDto dto) {
    return post("/auth/register", getHeaders(), dto);
  }

  protected ResponseEntity<ApiResponse<Void>> login(LoginUserDto dto) {
    return post("/auth/login", getHeaders(), dto);
  }
}
