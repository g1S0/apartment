package org.apartment.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apartment.exception.FileCountExceededException;
import org.apartment.exception.FileReadException;
import org.apartment.exception.FileSizeExceededException;
import org.apartment.exception.FileValidationException;
import org.apartment.exception.InvalidFileExtensionException;
import org.apartment.exception.InvalidFileNameException;
import org.apartment.exception.InvalidImageFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidationService {
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg");
  private static final int MAX_FILES = 3;

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
