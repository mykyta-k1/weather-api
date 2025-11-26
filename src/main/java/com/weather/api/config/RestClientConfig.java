package com.weather.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient(
      RestClient.Builder builder, @Value("${api.base-url}") String baseApiUrl) {
    return builder
        .baseUrl(baseApiUrl)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

}
