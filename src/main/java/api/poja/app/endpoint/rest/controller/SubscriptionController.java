package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.CourseSubscribed;
import api.poja.app.entity.Course;
import api.poja.app.entity.User;
import api.poja.app.repository.CourseRepository;
import api.poja.app.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class SubscriptionController {
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final EventProducer<CourseSubscribed> eventProducer;

  @PostMapping("/users/{userId}/courses/{courseId}/subscribe")
  public ResponseEntity<String> subscribe(@PathVariable UUID userId, @PathVariable UUID courseId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

    user.getCourses().add(course);
    userRepository.save(user);

    var event =
        CourseSubscribed.builder()
            .userId(user.getId())
            .userEmail(user.getEmail())
            .courseTitle(course.getTitle())
            .build();
    eventProducer.accept(List.of(event));

    return ResponseEntity.ok("Subscribed");
  }
}
