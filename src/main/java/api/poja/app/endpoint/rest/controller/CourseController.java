package api.poja.app.endpoint.rest.controller;

import api.poja.app.entity.Course;
import api.poja.app.endpoint.rest.model.CreateCourseRequest;
import api.poja.app.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseRepository courseRepository;

  @PostMapping("/courses")
  public ResponseEntity<Course> createCourse(@RequestBody CreateCourseRequest request) {
    var course = new Course();
    course.setTitle(request.title());
    course.setStartDate(request.startDate());
    course.setEndDate(request.endDate());

    Course saved = courseRepository.save(course);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/courses")
  public List<Course> getCourses() {
    return courseRepository.findAll();
  }

  @GetMapping("/courses/{courseId}")
  public Course getCourse(@PathVariable UUID courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
  }
}
