package hei.student.school.endpoint.rest.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import hei.student.school.model.Projection;
import hei.student.school.service.ProjectionService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(produces = APPLICATION_JSON_VALUE)
public class ProjectionController {
  private final ProjectionService service;

  @GetMapping("/projections")
  public List<Projection> listProjections() {
    return service.findAll();
  }
}
