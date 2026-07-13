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

    var pdfFile = pdfGenerator.generate(event.getUserEmail(), event.getCourseTitle(), message);
    var bucketKey = "subscriptions/" + event.getUserId() + "-" + UUID.randomUUID() + ".pdf";
    bucketComponent.upload(pdfFile, bucketKey);
    var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(30));

    InternetAddress recipient = new InternetAddress(event.getUserEmail());

    String htmlBody =
        """
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Confirmation d'inscription</title>
</head>
<body style="margin: 0; padding: 40px 0; background-color: #f4f6f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">

  <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 560px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 28, 76, 0.05); border: 1px solid #eef2f6;">
    <tr>
      <td align="center" style="background-color: #001c4c; padding: 40px 32px; border-bottom: 4px solid #dda625;">
        <div style="color: #dda625; font-size: 28px; font-weight: bold; font-family: 'Georgia', serif; letter-spacing: 2px; margin-bottom: 12px;">HEI</div>
        <h1 style="color: #ffffff; font-size: 22px; font-weight: 600; margin: 0; letter-spacing: -0.01em;">Inscription confirmée</h1>
      </td>
    </tr>
    <tr>
      <td style="padding: 40px 40px 32px 40px;">
        <p style="font-size: 15px; line-height: 1.6; color: #1e293b; margin: 0 0 16px 0;">Bonjour,</p>
        <p style="font-size: 15px; line-height: 1.6; color: #334155; margin: 0 0 32px 0;">
          Nous vous confirmons que vous êtes bien inscrit au cours :<br/>
          <strong style="color: #001c4c; font-size: 16px; display: inline-block; margin-top: 6px;">__COURSE_TITLE__</strong>
        </p>
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr>
            <td align="center">
              <a href="__PRESIGNED_URL__" style="background-color: #dda625; color: #001c4c; text-decoration: none; padding: 14px 32px; border-radius: 6px; font-size: 14px; font-weight: 700; display: inline-block; letter-spacing: 0.03em; box-shadow: 0 2px 6px rgba(221, 166, 37, 0.2);">
                Télécharger votre confirmation (PDF)
              </a>
            </td>
          </tr>
        </table>
        <table border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-top: 32px;">
          <tr>
            <td style="background-color: #f8fafc; border-left: 3px solid #cbd5e1; padding: 12px 16px; border-radius: 0 6px 6px 0;">
              <p style="font-size: 12px; color: #64748b; margin: 0; line-height: 1.5;">
                <em>Ce lien de téléchargement unique est valable uniquement pendant 30 minutes.</em>
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr>
      <td style="padding: 0 40px;">
        <div style="border-top: 1px solid #edf2f7;"></div>
      </td>
    </tr>
    <tr>
      <td align="center" style="padding: 24px 40px 40px 40px;">
        <p style="font-size: 12px; line-height: 1.5; color: #94a3b8; margin: 0;">
          Cet e-mail a été envoyé automatiquement par les systèmes de formation HEI.<br/>Merci de ne pas y répondre directement.
        </p>
      </td>
    </tr>
  </table>

</body>
</html>
"""
            .replace("__COURSE_TITLE__", escapeHtml(event.getCourseTitle()))
            .replace("__PRESIGNED_URL__", escapeHtml(presignedUrl.toString()));

    mailer.accept(
        new Email(
            recipient, List.of(), List.of(), "Confirmation d'inscription", htmlBody, List.of()));
  }

  private String escapeHtml(String s) {
    return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
