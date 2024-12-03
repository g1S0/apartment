package org.apartment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.apartment.exception.FileCountExceededException;
import org.apartment.exception.FileReadException;
import org.apartment.exception.FileSizeExceededException;
import org.apartment.exception.InvalidFileExtensionException;
import org.apartment.exception.InvalidFileNameException;
import org.apartment.exception.InvalidImageFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class UploadServiceTest {
  @Mock
  private S3Client s3Client;

  @Mock
  private MultipartFile file;

  private UploadService uploadService;

  private final String s3StorageEndpoint = "http://localhost:9000";
  private final String bucketName = "test-bucket";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    uploadService = spy(new UploadService(s3Client, s3StorageEndpoint, bucketName));
  }

  @Test
  void testUploadFiles_success() throws Exception {
    String fileName = "image.jpg";
    byte[] content = "test content".getBytes();

    when(file.getOriginalFilename()).thenReturn(fileName);
    when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(content));
    when(file.getSize()).thenReturn((long) content.length);

    doNothing().when(uploadService).validateFiles(any(MultipartFile[].class));

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

    List<String> result = uploadService.uploadFiles(new MultipartFile[] {file});

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0).contains(bucketName));
  }

  @Test
  void testUploadFiles_exceedsFileCount() {
    MultipartFile[] files = new MultipartFile[4];
    for (int i = 0; i < 4; i++) {
      files[i] = mock(MultipartFile.class);
    }

    assertThrows(FileCountExceededException.class, () -> uploadService.uploadFiles(files));
  }

  @Test
  void testUploadFiles_invalidImage() throws IOException {
    MultipartFile invalidFile = mock(MultipartFile.class);
    when(invalidFile.getOriginalFilename()).thenReturn("image.jpg");
    when(invalidFile.getSize()).thenReturn(1024L);

    InputStream mockInputStream = mock(InputStream.class);
    when(invalidFile.getInputStream()).thenReturn(mockInputStream);

    try (MockedStatic<ImageIO> mockedImageIO = Mockito.mockStatic(ImageIO.class)) {
      mockedImageIO.when(() -> ImageIO.read(mockInputStream)).thenReturn(null);

      assertThrows(InvalidImageFileException.class, () -> {
        uploadService.uploadFiles(new MultipartFile[] {invalidFile});
      });
    }
  }

  @Test
  void testUploadFiles_errorReadingImage() throws IOException {
    MultipartFile invalidFile = mock(MultipartFile.class);
    when(invalidFile.getOriginalFilename()).thenReturn("image.jpg");
    when(invalidFile.getSize()).thenReturn(1024L);

    InputStream mockInputStream = mock(InputStream.class);
    when(invalidFile.getInputStream()).thenReturn(mockInputStream);

    try (MockedStatic<ImageIO> mockedImageIO = Mockito.mockStatic(ImageIO.class)) {
      mockedImageIO.when(() -> ImageIO.read(mockInputStream)).thenThrow(IOException.class);

      assertThrows(FileReadException.class, () -> {
        uploadService.uploadFiles(new MultipartFile[] {invalidFile});
      });
    }
  }

  @Test
  void testUploadFiles_emptyFileName() throws IOException {
    MultipartFile invalidFile = mock(MultipartFile.class);
    when(invalidFile.getOriginalFilename()).thenReturn("");
    when(invalidFile.getSize()).thenReturn(1024L);

    InputStream mockInputStream = mock(InputStream.class);
    when(invalidFile.getInputStream()).thenReturn(mockInputStream);

    try (MockedStatic<ImageIO> mockedImageIO = Mockito.mockStatic(ImageIO.class)) {
      mockedImageIO.when(() -> ImageIO.read(mockInputStream))
          .thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));

      assertThrows(InvalidFileNameException.class, () -> {
        uploadService.uploadFiles(new MultipartFile[] {invalidFile});
      });
    }
  }

  @Test
  void testUploadFiles_invalidFileType() throws IOException {
    MultipartFile invalidFile = mock(MultipartFile.class);
    when(invalidFile.getOriginalFilename()).thenReturn("file.txt");
    when(invalidFile.getSize()).thenReturn(1024L);

    InputStream mockInputStream = mock(InputStream.class);
    when(invalidFile.getInputStream()).thenReturn(mockInputStream);

    try (MockedStatic<ImageIO> mockedImageIO = Mockito.mockStatic(ImageIO.class)) {
      mockedImageIO.when(() -> ImageIO.read(mockInputStream))
          .thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));

      assertThrows(InvalidFileExtensionException.class, () -> {
        uploadService.uploadFiles(new MultipartFile[] {invalidFile});
      });
    }
  }

  @Test
  void testUploadFiles_fileSizeExceeded() throws IOException {
    MultipartFile largeFile = mock(MultipartFile.class);
    when(largeFile.getOriginalFilename()).thenReturn("image.jpg");
    when(largeFile.getSize()).thenReturn(6L * 1024 * 1024);

    InputStream mockInputStream = mock(InputStream.class);
    when(largeFile.getInputStream()).thenReturn(mockInputStream);

    try (MockedStatic<ImageIO> mockedImageIO = Mockito.mockStatic(ImageIO.class)) {
      mockedImageIO.when(() -> ImageIO.read(mockInputStream))
          .thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));

      assertThrows(FileSizeExceededException.class, () -> {
        uploadService.uploadFiles(new MultipartFile[] {largeFile});
      });
    }
  }
}