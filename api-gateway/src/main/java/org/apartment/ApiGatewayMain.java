package org.apartment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ApiGatewayMain {
  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayMain.class, args);
  }
}