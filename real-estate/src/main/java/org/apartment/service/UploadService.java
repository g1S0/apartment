package org.apartment.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class UploadService {
  private final S3Client s3Client;
  private final String s3StorageEndpoint;
  private final String bucketName;
  private final FileValidationService fileValidationService;

  @Autowired
  public UploadService(S3Client s3Client, @Value("${aws.s3.endpoint}") String s3StorageEndpoint,
                       @Value("${aws.s3.bucket-name}") String bucketName,
                       FileValidationService fileValidationService) {
    this.s3Client = s3Client;
    this.s3StorageEndpoint = s3StorageEndpoint;
    this.bucketName = bucketName;
    this.fileValidationService = fileValidationService;
  }

  public List<String> uploadFiles(MultipartFile[] files) {
    log.info("Starting file upload. Total files to upload: {}", files.length);
    fileValidationService.validateFiles(files);
    log.info("File validation completed successfully.");

    List<CompletableFuture<String>> uploadFutures =
        Arrays.stream(files).map(file -> CompletableFuture.supplyAsync(() -> {
          String originalFilename = file.getOriginalFilename();
          try {
            log.info("Uploading file: {}", originalFilename);

            String uniqueFileName = UUID.randomUUID().toString();

            PutObjectRequest putObjectRequest =
                PutObjectRequest.builder().bucket(bucketName).key(uniqueFileName).build();

            try (InputStream inputStream = file.getInputStream()) {
              log.debug("Starting upload to S3 for file: {}", originalFilename);
              s3Client.putObject(putObjectRequest,
                  RequestBody.fromInputStream(inputStream, file.getSize()));
              log.debug("Upload completed for file: {}", originalFilename);
            }

            String fileUrl =
                String.format("%s/%s/%s", s3StorageEndpoint, bucketName, uniqueFileName);
            log.info("File uploaded successfully. File URL: {}", fileUrl);

            return fileUrl;
          } catch (Exception e) {
            log.error("Error while uploading file: {}", originalFilename, e);
            throw new RuntimeException("Error while uploading file: " + originalFilename, e);
          }
        })).toList();

    log.info("Waiting for all uploads to complete...");
    CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();
    log.info("All file uploads completed successfully.");

    List<String> fileUrls =
        uploadFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());

    log.info("Returning file URLs: {}", fileUrls);
    return fileUrls;
  }

  public void deleteFiles(List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) {
      log.warn("No image URLs provided for deletion.");
      return;
    }

    log.info("Preparing to delete {} images from S3.", imageUrls.size());

    List<ObjectIdentifier> objectsToDelete = imageUrls.stream()
        .map(this::extractKeyFromUrl)
        .map(key -> ObjectIdentifier.builder().key(key).build())
        .collect(Collectors.toList());

    log.info("Generated {} objects to delete from S3.", objectsToDelete.size());

    DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
        .bucket(bucketName)
        .delete(Delete.builder().objects(objectsToDelete).build())
        .build();

    s3Client.deleteObjects(deleteObjectsRequest);
    log.info("Successfully requested deletion of {} objects from S3.", objectsToDelete.size());
  }

  private String extractKeyFromUrl(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }
}
