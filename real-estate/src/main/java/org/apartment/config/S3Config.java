package org.apartment.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config
{

  @Value("${aws.s3.access-key}")
  private String accessKey;

  @Value("${aws.s3.secret-key}")
  private String secretKey;

  @Value("${aws.s3.region}")
  private String region;

  @Value("${aws.s3.endpoint}")
  private String endpoint;

  @Bean
  public S3Client s3Client()
  {
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(() -> credentials)
        .endpointOverride(URI.create(endpoint))
        .serviceConfiguration(s3Config -> s3Config.pathStyleAccessEnabled(true))
        .build();
  }
}
