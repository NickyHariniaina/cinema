package hei.student.school.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import hei.student.school.model.AuthPayload;
import hei.student.school.model.UserWithToken;
import hei.student.school.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class AuthController {
  private final UserService service;

  @PostMapping(value = "/register")
  @ResponseStatus(CREATED)
  public UserWithToken register(@RequestBody AuthPayload payload) {
    return service.register(payload);
  }

  @PostMapping(value = "/login")
  public UserWithToken login(@RequestBody AuthPayload payload) {
    return service.login(payload);
  }
}
