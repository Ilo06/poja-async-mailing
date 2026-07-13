package api.poja.app.endpoint.rest.controller;

import static java.io.File.createTempFile;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.ImageSubmitted;
import api.poja.app.entity.Image;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.repository.ImageRepository;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class ImageController {

  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

  private final ImageRepository imageRepository;
  private final BucketComponent bucketComponent;
  private final EventProducer<ImageSubmitted> eventProducer;

  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Image> submitImage(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email)
      throws IOException {
    validateContentType(file);

    var image = new Image();
    image.setFileName(file.getOriginalFilename());
    image.setUserEmail(email);
    Image saved = imageRepository.save(image);

    File tempFile = toTempFile(file);
    String originalBucketKey = "images/original/" + saved.getId() + "-" + saved.getFileName();
    bucketComponent.upload(tempFile, originalBucketKey);

    var event =
        ImageSubmitted.builder()
            .imageId(saved.getId())
            .fileName(saved.getFileName())
            .userEmail(saved.getUserEmail())
            .originalBucketKey(originalBucketKey)
            .build();
    eventProducer.accept(List.of(event));

    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/images")
  public List<Image> getImages() {
    return imageRepository.findAll();
  }

  private void validateContentType(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Only JPEG or PNG images are allowed");
    }
  }

  private File toTempFile(MultipartFile file) throws IOException {
    String originalFilename = file.getOriginalFilename();
    String suffix =
        originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf('.'))
            : "";
    File temp = createTempFile("upload-", suffix);
    file.transferTo(temp);
    return temp;
  }
}
