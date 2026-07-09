package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.CourseSubscribed;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import api.poja.app.service.SubscriptionPdfGenerator;
import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseSubscribedService implements Consumer<CourseSubscribed> {
  private final Mailer mailer;
  private final BucketComponent bucketComponent;
  private final SubscriptionPdfGenerator pdfGenerator;

  @SneakyThrows
  @Override
  public void accept(CourseSubscribed event) {
    String message = "Vous êtes bien inscrit au cours : " + event.getCourseTitle();

    var pdfFile = pdfGenerator.generate(event.getUserEmail(), message);
    var bucketKey = "subscriptions/" + event.getUserId() + "-" + UUID.randomUUID() + ".pdf";
    bucketComponent.upload(pdfFile, bucketKey);
    var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(30));

    InternetAddress recipient = new InternetAddress(event.getUserEmail());
    String htmlBody =
        "<p>Bonjour "
            + event.getUserEmail()
            + ",</p>"
            + "<p>"
            + message
            + "</p>"
            + "<p>Votre confirmation d'inscription au format PDF est disponible ici : "
            + "<a href=\""
            + presignedUrl
            + "\">Télécharger le PDF</a></p>"
            + "<p><i>Ce lien est valable 30 minutes.</i></p>";

    mailer.accept(
        new Email(
            recipient, List.of(), List.of(), "Confirmation d'inscription", htmlBody, List.of()));
  }
}
