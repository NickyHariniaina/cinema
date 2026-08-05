package hei.student.school.endpoint.rest.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.service.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class MovieController {
  private final MovieService service;

  @PutMapping("/movies")
  public Movie upsertMovie(@RequestBody MovieRequest request) {
    return service.save(request);
  }
}
