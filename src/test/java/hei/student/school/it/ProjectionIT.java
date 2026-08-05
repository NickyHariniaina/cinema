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
import hei.student.school.model.Projection;
import hei.student.school.model.ProjectionRequest;
import hei.student.school.model.UserRole;
import hei.student.school.model.UserWithToken;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JRoomRepository;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.model.JRoom;
import hei.student.school.repository.model.JUser;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

class ProjectionIT extends FacadeIT {

  private static final String PROJECTIONS_URL = "/projections";
  private static final String MOVIES_URL = "/movies";
  private static final String LOGIN_URL = "/login";
  private static final String PASSWORD = "CinemaPass123!";
  private static final Instant DATETIME = Instant.parse("2026-08-06T20:00:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("10.5");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JUserRepository jUserRepository;
  @Autowired JMovieRepository jMovieRepository;
  @Autowired JProjectionRepository jProjectionRepository;
  @Autowired JRoomRepository jRoomRepository;
  @Autowired PasswordEncoder passwordEncoder;

  private HttpHeaders managerHeaders;
  private HttpHeaders clientHeaders;
  private HttpHeaders employeeHeaders;

  @BeforeEach
  void setUp() {
    jProjectionRepository.deleteAll();
    jMovieRepository.deleteAll();
    jRoomRepository.deleteAll();
    managerHeaders = authHeaders(seedUser("manager@cinema.test", UserRole.MANAGER).getToken());
    clientHeaders = authHeaders(seedUser("client@cinema.test", UserRole.CLIENT).getToken());
    employeeHeaders = authHeaders(seedUser("employee@cinema.test", UserRole.EMPLOYEE).getToken());
  }

  @Test
  void put_projections_creates_projection_and_is_listable() {
    var movie = putForObject(MOVIES_URL, movieRequest("Inception"), Movie.class);
    var room = jRoomRepository.save(new JRoom(null, "1", 50));

    var created =
        putForObject(
            PROJECTIONS_URL,
            new ProjectionRequest(movie.getId(), room.getId(), DATETIME, SEAT_PRICE),
            Projection.class);

    assertNotNull(created.getId());
    assertEquals(movie.getId(), created.getMovie().getId());
    assertEquals(room.getId(), created.getRoom().getId());
    assertEquals(DATETIME, created.getDatetime());
    assertEquals(SEAT_PRICE, created.getSeatPrice());

    var response =
        testRestTemplate.exchange(
            PROJECTIONS_URL,
            GET,
            new HttpEntity<>(jsonHeaders()),
            new ParameterizedTypeReference<List<Projection>>() {});
    assertEquals(OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertEquals(created.getId(), response.getBody().get(0).getId());
  }

  @Test
  void put_projections_is_forbidden_for_client_and_employee() {
    var movie = putForObject(MOVIES_URL, movieRequest("Inception"), Movie.class);
    var room = jRoomRepository.save(new JRoom(null, "1", 50));
    var request = new ProjectionRequest(movie.getId(), room.getId(), DATETIME, SEAT_PRICE);

    var clientResponse =
        testRestTemplate.exchange(
            PROJECTIONS_URL, PUT, new HttpEntity<>(request, clientHeaders), Map.class);
    var employeeResponse =
        testRestTemplate.exchange(
            PROJECTIONS_URL, PUT, new HttpEntity<>(request, employeeHeaders), Map.class);

    assertEquals(FORBIDDEN, clientResponse.getStatusCode());
    assertEquals(FORBIDDEN, employeeResponse.getStatusCode());
    assertEquals("403 FORBIDDEN", clientResponse.getBody().get("type"));
    assertEquals("403 FORBIDDEN", employeeResponse.getBody().get("type"));
  }

  @Test
  void put_projections_returns_401_when_unauthenticated() {
    var response =
        testRestTemplate.exchange(
            PROJECTIONS_URL, PUT, new HttpEntity<>(Map.of(), jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertEquals("401 UNAUTHORIZED", response.getBody().get("type"));
  }

  @Test
  void put_projections_rejects_unknown_movie() {
    var room = jRoomRepository.save(new JRoom(null, "1", 50));
    var response =
        testRestTemplate.exchange(
            PROJECTIONS_URL,
            PUT,
            new HttpEntity<>(
                new ProjectionRequest(UUID.randomUUID(), room.getId(), DATETIME, SEAT_PRICE),
                managerHeaders),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals("400 BAD_REQUEST", response.getBody().get("type"));
  }

  @Test
  void put_projections_rejects_unknown_room() {
    var movie = putForObject(MOVIES_URL, movieRequest("Inception"), Movie.class);
    var response =
        testRestTemplate.exchange(
            PROJECTIONS_URL,
            PUT,
            new HttpEntity<>(
                new ProjectionRequest(movie.getId(), UUID.randomUUID(), DATETIME, SEAT_PRICE),
                managerHeaders),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals("400 BAD_REQUEST", response.getBody().get("type"));
  }

  @Test
  void put_projections_rejects_missing_fields() {
    var movie = putForObject(MOVIES_URL, movieRequest("Inception"), Movie.class);
    var room = jRoomRepository.save(new JRoom(null, "1", 50));
    var response =
        testRestTemplate.exchange(
            PROJECTIONS_URL,
            PUT,
            new HttpEntity<>(
                new ProjectionRequest(movie.getId(), room.getId(), null, null), managerHeaders),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals(
        "Datetime is mandatory. SeatPrice is mandatory", response.getBody().get("message"));
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

  private static MovieRequest movieRequest(String title) {
    return new MovieRequest(
        title, Set.of(Genre.ACTION), "A movie about " + title, Duration.ofHours(2));
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
