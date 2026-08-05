package hei.student.school.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.student.school.model.Genre;
import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.security.jwt.JwtService;
import hei.student.school.service.MovieService;
import hei.student.school.service.UserService;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

  private static final String MOVIES_URL = "/movies";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean MovieService movieService;
  @MockBean UserService userService;
  @MockBean JwtService jwtService;

  @Test
  void get_movies_returns_200_with_all_movies() throws Exception {
    when(movieService.findAll()).thenReturn(List.of(movie()));

    mockMvc
        .perform(get(MOVIES_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Inception"))
        .andExpect(jsonPath("$[0].genres[0]").value("ACTION"));
  }

  @Test
  void put_movies_returns_200_with_saved_movie() throws Exception {
    var request =
        new MovieRequest("Inception", Set.of(Genre.ACTION), "Dream heist", Duration.ofHours(2));
    when(movieService.save(any(MovieRequest.class))).thenReturn(movie());

    mockMvc
        .perform(
            put(MOVIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.title").value("Inception"));
  }

  @Test
  void put_movies_returns_400_when_payload_invalid() throws Exception {
    when(movieService.save(any(MovieRequest.class)))
        .thenThrow(
            new IllegalArgumentException(
                "Title is mandatory. Genres are mandatory. Description is mandatory. Duration is"
                    + " mandatory"));

    mockMvc
        .perform(put(MOVIES_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("400 BAD_REQUEST"))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "Title is mandatory. Genres are mandatory. Description is mandatory. Duration"
                        + " is mandatory"));
  }

  private static Movie movie() {
    var movie = new Movie();
    movie.setId(UUID.randomUUID());
    movie.setTitle("Inception");
    movie.setGenres(Set.of(Genre.ACTION));
    movie.setDescription("Dream heist");
    movie.setDuration(Duration.ofHours(2).plusMinutes(28));
    return movie;
  }
}
