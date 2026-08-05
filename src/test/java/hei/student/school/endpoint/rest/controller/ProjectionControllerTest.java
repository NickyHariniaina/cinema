package hei.student.school.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.school.model.Movie;
import hei.student.school.model.Projection;
import hei.student.school.model.ProjectionRequest;
import hei.student.school.security.jwt.JwtService;
import hei.student.school.service.ProjectionService;
import hei.student.school.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectionControllerTest {

  private static final String PROJECTIONS_URL = "/projections";

  @Autowired MockMvc mockMvc;
  @MockBean ProjectionService projectionService;
  @MockBean UserService userService;
  @MockBean JwtService jwtService;

  @Test
  void get_projections_returns_200_with_all_projections() throws Exception {
    when(projectionService.findAll()).thenReturn(List.of(projection()));

    mockMvc
        .perform(get(PROJECTIONS_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].movie.title").value("Inception"));
  }

  @Test
  void put_projections_returns_200_with_created_projection() throws Exception {
    var created = projection();
    when(projectionService.create(any(ProjectionRequest.class))).thenReturn(created);

    mockMvc
        .perform(
            put(PROJECTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"movieId":"%s","roomId":"%s","datetime":"2026-08-06T20:00:00Z","seatPrice":10.5}
"""
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.movie.title").value("Inception"));
  }

  @Test
  void put_projections_returns_400_when_invalid_request() throws Exception {
    when(projectionService.create(any(ProjectionRequest.class)))
        .thenThrow(
            new IllegalArgumentException("Room with id " + UUID.randomUUID() + " not found"));

    mockMvc
        .perform(
            put(PROJECTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"movieId":"%s","roomId":"%s","datetime":"2026-08-06T20:00:00Z","seatPrice":10.5}
"""
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("400 BAD_REQUEST"));
  }

  @Test
  void put_projections_returns_403_when_access_denied() throws Exception {
    when(projectionService.create(any(ProjectionRequest.class)))
        .thenThrow(new AccessDeniedException("Forbidden"));

    mockMvc
        .perform(
            put(PROJECTIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
{"movieId":"%s","roomId":"%s","datetime":"2026-08-06T20:00:00Z","seatPrice":10.5}
"""
                        .formatted(UUID.randomUUID(), UUID.randomUUID())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("403 FORBIDDEN"));
  }

  private static Projection projection() {
    var movie = new Movie();
    movie.setId(UUID.randomUUID());
    movie.setTitle("Inception");
    var projection = new Projection();
    projection.setId(UUID.randomUUID());
    projection.setMovie(movie);
    return projection;
  }
}
