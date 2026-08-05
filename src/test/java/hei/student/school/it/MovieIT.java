package hei.student.school.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.student.school.conf.FacadeIT;
import hei.student.school.model.AuthPayload;
import hei.student.school.model.Genre;
import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.model.UserRole;
import hei.student.school.model.UserWithToken;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.model.JUser;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

class MovieIT extends FacadeIT {

  private static final String MOVIES_URL = "/movies";
  private static final String LOGIN_URL = "/login";
  private static final String PASSWORD = "CinemaPass123!";

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JUserRepository jUserRepository;
  @Autowired JMovieRepository jMovieRepository;
  @Autowired PasswordEncoder passwordEncoder;

  private HttpHeaders managerHeaders;

  @BeforeEach
  void setUp() {
    jMovieRepository.deleteAll();
    managerHeaders = authHeaders(seedUser("manager@cinema.test", UserRole.MANAGER).getToken());
  }

  @Test
  void put_movies_creates_movie() {
    var request =
        new MovieRequest(
            "Inception",
            Set.of(Genre.ACTION, Genre.THRILLER),
            "A thief who steals corporate secrets through dream-sharing",
            Duration.ofHours(2).plusMinutes(28));

    var created = putForObject(MOVIES_URL, request, Movie.class);

    assertNotNull(created.getId());
    assertEquals("Inception", created.getTitle());
    assertEquals(Set.of(Genre.ACTION, Genre.THRILLER), created.getGenres());
    assertEquals(
        "A thief who steals corporate secrets through dream-sharing", created.getDescription());
    assertEquals(Duration.ofHours(2).plusMinutes(28), created.getDuration());
    assertEquals(1, jMovieRepository.count());
  }

  @Test
  void put_movies_with_existing_title_updates_in_place() {
    var first =
        putForObject(
            MOVIES_URL,
            new MovieRequest(
                "Dune", Set.of(Genre.SCI_FI), "Desert planet", Duration.ofHours(2).plusMinutes(35)),
            Movie.class);

    var updated =
        putForObject(
            MOVIES_URL,
            new MovieRequest(
                "Dune", Set.of(Genre.SCI_FI, Genre.DRAMA), "Updated synopsis", Duration.ofHours(3)),
            Movie.class);

    assertEquals(first.getId(), updated.getId());
    assertEquals(Set.of(Genre.SCI_FI, Genre.DRAMA), updated.getGenres());
    assertEquals("Updated synopsis", updated.getDescription());
    assertEquals(Duration.ofHours(3), updated.getDuration());
    assertEquals(1, jMovieRepository.count());
  }

  @Test
  void get_movies_returns_empty_list_when_none() {
    var response =
        testRestTemplate.exchange(
            MOVIES_URL,
            GET,
            new HttpEntity<>(managerHeaders),
            new ParameterizedTypeReference<List<Movie>>() {});

    assertEquals(OK, response.getStatusCode());
    assertEquals(List.of(), response.getBody());
  }

  @Test
  void get_movies_returns_all_movies_for_manager() {
    var avatar =
        putForObject(
            MOVIES_URL,
            new MovieRequest("Avatar", Set.of(Genre.SCI_FI), "Blue aliens", Duration.ofHours(3)),
            Movie.class);
    var titanic =
        putForObject(
            MOVIES_URL,
            new MovieRequest(
                "Titanic",
                Set.of(Genre.DRAMA),
                "A ship sinks",
                Duration.ofHours(3).plusMinutes(14)),
            Movie.class);

    var response =
        testRestTemplate.exchange(
            MOVIES_URL,
            GET,
            new HttpEntity<>(managerHeaders),
            new ParameterizedTypeReference<List<Movie>>() {});

    assertEquals(OK, response.getStatusCode());
    var movies = response.getBody();
    assertNotNull(movies);
    assertEquals(2, movies.size());
    var byTitle = movies.stream().collect(Collectors.toMap(Movie::getTitle, movie -> movie));
    assertEquals(avatar.getId(), byTitle.get("Avatar").getId());
    assertEquals(Set.of(Genre.SCI_FI), byTitle.get("Avatar").getGenres());
    assertEquals(titanic.getId(), byTitle.get("Titanic").getId());
    assertEquals(Duration.ofHours(3).plusMinutes(14), byTitle.get("Titanic").getDuration());
  }

  @Test
  void get_movies_returns_401_when_unauthenticated() {
    var response =
        testRestTemplate.exchange(MOVIES_URL, GET, new HttpEntity<>(jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertEquals("401 UNAUTHORIZED", response.getBody().get("type"));
  }

  @Test
  void put_movies_rejects_missing_fields() {
    var response =
        testRestTemplate.exchange(
            MOVIES_URL, PUT, new HttpEntity<>(Map.of(), managerHeaders), Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals(
        "Title is mandatory. Genres are mandatory. Description is mandatory. Duration is mandatory",
        response.getBody().get("message"));
  }

  @Test
  void put_movies_is_forbidden_for_client_and_employee() {
    var clientHeaders = authHeaders(seedUser("client@cinema.test", UserRole.CLIENT).getToken());
    var employeeHeaders =
        authHeaders(seedUser("employee@cinema.test", UserRole.EMPLOYEE).getToken());
    var request = new MovieRequest("Dune", Set.of(Genre.SCI_FI), "x", Duration.ofHours(2));

    var clientResponse =
        testRestTemplate.exchange(
            MOVIES_URL, PUT, new HttpEntity<>(request, clientHeaders), Map.class);
    var employeeResponse =
        testRestTemplate.exchange(
            MOVIES_URL, PUT, new HttpEntity<>(request, employeeHeaders), Map.class);

    assertEquals(FORBIDDEN, clientResponse.getStatusCode());
    assertEquals(FORBIDDEN, employeeResponse.getStatusCode());
    assertEquals("403 FORBIDDEN", clientResponse.getBody().get("type"));
    assertEquals("403 FORBIDDEN", employeeResponse.getBody().get("type"));
  }

  @Test
  void put_movies_returns_401_when_unauthenticated() {
    var response =
        testRestTemplate.exchange(
            MOVIES_URL, PUT, new HttpEntity<>(Map.of(), jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertEquals("401 UNAUTHORIZED", response.getBody().get("type"));
  }

  private UserWithToken seedUser(String email, UserRole role) {
    jUserRepository.findByEmail(email).ifPresent(jUserRepository::delete);
    jUserRepository.save(
        new JUser(null, null, null, null, email, passwordEncoder.encode(PASSWORD), null, role));
    return login(email, PASSWORD);
  }

  private UserWithToken login(String email, String password) {
    return testRestTemplate.postForObject(
        LOGIN_URL, new AuthPayload(email, password), UserWithToken.class);
  }

  private <T> T putForObject(String url, Object body, Class<T> responseType) {
    return testRestTemplate
        .exchange(url, PUT, new HttpEntity<>(body, managerHeaders), responseType)
        .getBody();
  }

  private static HttpHeaders authHeaders(String token) {
    var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private static HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return headers;
  }
}
