package com.trusthold.shared.dtos;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.aop.proxy-target-class=false"
)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
public abstract class IntegrationTestSupport {

  @Autowired
  protected TestRestTemplate restTemplate;

  protected HttpHeaders getHeaders(){
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private <T> HttpEntity<T> getHttpEntity(T body, HttpHeaders headers){
    return new HttpEntity<>(body, headers);
  }

  protected <T> ResponseEntity<ApiResponse<T>> get(String endpoint, HttpHeaders headers, Object body) {
    return restTemplate.exchange(endpoint, HttpMethod.GET, getHttpEntity(body, headers), new ParameterizedTypeReference<>() {});
  }

  protected ResponseEntity<ApiResponse<Void>> post(String endpoint, HttpHeaders headers, Object body) {
    return restTemplate.exchange(endpoint, HttpMethod.POST,
        new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
  }

  protected ResponseEntity<ApiResponse<Void>> register(RegisterUserDto dto) {
    return post("/auth/register", getHeaders(), dto);
  }

  protected ResponseEntity<ApiResponse<Void>> login(LoginUserDto dto) {
    return post("/auth/login", getHeaders(), dto);
  }
}
