package org.apartment.service;

import org.apartment.exception.FileValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class UploadService {
    private final S3Client s3Client;
    private final String s3StorageEndpoint;
    private final String bucketName;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg");

    @Autowired
    public UploadService(S3Client s3Client,
                         @Value("${aws.s3.endpoint}") String s3StorageEndpoint,
                         @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.s3StorageEndpoint = s3StorageEndpoint;
        this.bucketName = bucketName;
    }

    public List<String> uploadFiles(MultipartFile[] files) throws Exception {
        validateFiles(files);

        List<CompletableFuture<String>> uploadFutures = Arrays.stream(files)
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    String originalFilename = file.getOriginalFilename();
                    try {
                        String uniqueFileName = UUID.randomUUID() + "-" + originalFilename;

                        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(uniqueFileName)
                                .build();

                        try (InputStream inputStream = file.getInputStream()) {
                            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
                        }

                        return String.format("%s/%s/%s", s3StorageEndpoint, bucketName, uniqueFileName);
                    } catch (Exception e) {
                        throw new RuntimeException("Error while uploading file: " + originalFilename, e);
                    }
                }))
                .toList();

        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

        return uploadFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private void validateFiles(MultipartFile[] files) throws FileValidationException {
        if (files.length > 3) {
            throw new FileValidationException("Maximum amount of files exceeded");
        }

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            long maxFileSize = 5 * 1024 * 1024; // 5 MB

            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    throw new FileValidationException("Invalid image file");
                }
            } catch (IOException e) {
                throw new FileValidationException("Error reading the image file");
            }

            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new FileValidationException("File name should be valid");
            }

            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                throw new FileValidationException("Only JPG and JPEG formats are allowed");
            }

            if (file.getSize() > maxFileSize) {
                throw new FileValidationException("File size exceeds the maximum allowed size of 5 MB");
            }
        }
    }
}
