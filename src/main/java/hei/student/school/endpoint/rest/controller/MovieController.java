package hei.student.school.endpoint.rest.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.service.MovieService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping
public class MovieController {
  private final MovieService service;

  @GetMapping("/movies")
  public List<Movie> listMovies() {
    return service.findAll();
  }

  @PutMapping("/movies")
  public Movie upsertMovie(@RequestBody MovieRequest request) {
    return service.save(request);
  }
}
