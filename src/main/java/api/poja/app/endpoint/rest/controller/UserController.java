package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.CreateUserRequest;
import api.poja.app.entity.User;
import api.poja.app.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class UserController {
  private final UserRepository userRepository;

  @PostMapping("/users")
  public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
    var user = new User();
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setUserName(request.userName());
    user.setEmail(request.email());

    User saved = userRepository.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/users")
  public List<User> getUsers() {
    return userRepository.findAll();
  }

  @GetMapping("/users/{userId}")
  public User getUser(@PathVariable UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
