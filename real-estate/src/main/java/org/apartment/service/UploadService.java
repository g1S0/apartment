package org.apartment.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apartment.exception.FileCountExceededException;
import org.apartment.exception.FileReadException;
import org.apartment.exception.FileSizeExceededException;
import org.apartment.exception.FileValidationException;
import org.apartment.exception.InvalidFileExtensionException;
import org.apartment.exception.InvalidFileNameException;
import org.apartment.exception.InvalidImageFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class UploadService {
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg");
  private static final int MAX_FILES = 3;
  private final S3Client s3Client;
  private final String s3StorageEndpoint;
  private final String bucketName;

  @Autowired
  public UploadService(S3Client s3Client, @Value("${aws.s3.endpoint}") String s3StorageEndpoint,
                       @Value("${aws.s3.bucket-name}") String bucketName) {
    this.s3Client = s3Client;
    this.s3StorageEndpoint = s3StorageEndpoint;
    this.bucketName = bucketName;
  }

  public List<String> uploadFiles(MultipartFile[] files) {
    log.info("Starting file upload. Total files to upload: {}", files.length);
    validateFiles(files);
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

  void validateFiles(MultipartFile[] files) throws FileValidationException {
    if (files.length > MAX_FILES) {
      throw new FileCountExceededException("Maximum amount of files exceeded");
    }

    for (MultipartFile file : files) {
      String originalFileName = file.getOriginalFilename();
      final long maxFileSize = 5 * 1024 * 1024; // 5 MB

      try {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
          throw new InvalidImageFileException("Invalid image file");
        }
      } catch (IOException e) {
        throw new FileReadException("Error reading the image file");
      }

      if (originalFileName == null || originalFileName.isEmpty()) {
        throw new InvalidFileNameException("File name should be valid");
      }

      String fileExtension =
          originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
      if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
        throw new InvalidFileExtensionException("Only JPG and JPEG formats are allowed");
      }

      if (file.getSize() > maxFileSize) {
        throw new FileSizeExceededException("File size exceeds the maximum allowed size of 5 MB");
      }
    }
  }
}
