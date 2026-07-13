package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.ImageSubmitted;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.file.image.ImageGrayscaleConverter;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImageSubmittedService implements Consumer<ImageSubmitted> {

  private final BucketComponent bucketComponent;
  private final ImageGrayscaleConverter grayscaleConverter;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(ImageSubmitted event) {
    var originalFile = bucketComponent.download(event.getOriginalBucketKey());
    var grayscaleFile = grayscaleConverter.convert(originalFile, event.getFileName());

    String processedBucketKey =
        "images/processed/" + event.getImageId() + "-" + event.getFileName();
    bucketComponent.upload(grayscaleFile, processedBucketKey);

    URL presignedUrl = bucketComponent.presign(processedBucketKey, Duration.ofDays(7));

    InternetAddress recipient = new InternetAddress(event.getUserEmail());
    String htmlBody =
        "<p>Hello,</p>"
            + "<p>Your image <strong>"
            + escapeHtml(event.getFileName())
            + "</strong> has been converted to black and white.</p>"
            + "<p><a href=\""
            + presignedUrl
            + "\">Download the image</a></p>"
            + "<p><em>This link is valid for 7 days.</em></p>";

    mailer.accept(
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Your black and white image is ready",
            htmlBody,
            List.of()));
  }

  private String escapeHtml(String s) {
    return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
