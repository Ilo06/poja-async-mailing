package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.CourseSubscribed;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseSubscribedService implements Consumer<CourseSubscribed> {
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(CourseSubscribed event) {
    InternetAddress recipient = new InternetAddress(event.getUserEmail());
    String body = "Vous êtes bien inscrit au cours : " + event.getCourseTitle();
    mailer.accept(
        new Email(recipient, List.of(), List.of(), "Confirmation d'inscription", body, List.of()));
  }
}
