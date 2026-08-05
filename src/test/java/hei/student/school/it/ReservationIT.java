package hei.student.school.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import hei.student.school.conf.FacadeIT;
import hei.student.school.model.AuthPayload;
import hei.student.school.model.CreateReservationRequest;
import hei.student.school.model.Genre;
import hei.student.school.model.Reservation;
import hei.student.school.model.ReservationStatus;
import hei.student.school.model.Seat;
import hei.student.school.model.UserRole;
import hei.student.school.model.UserWithToken;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JReservationRepository;
import hei.student.school.repository.JRoomRepository;
import hei.student.school.repository.JSeatRepository;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.model.JMovie;
import hei.student.school.repository.model.JProjection;
import hei.student.school.repository.model.JRoom;
import hei.student.school.repository.model.JSeat;
import hei.student.school.repository.model.JUser;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

class ReservationIT extends FacadeIT {

  private static final String RESERVATIONS_URL = "/reservations";
  private static final String LOGIN_URL = "/login";
  private static final String PASSWORD = "CinemaPass123!";
  private static final Instant DATETIME = Instant.parse("2026-08-06T20:00:00Z");
  private static final BigDecimal SEAT_PRICE = new BigDecimal("10.5");

  @Autowired TestRestTemplate testRestTemplate;
  @Autowired JUserRepository jUserRepository;
  @Autowired JMovieRepository jMovieRepository;
  @Autowired JProjectionRepository jProjectionRepository;
  @Autowired JReservationRepository jReservationRepository;
  @Autowired JRoomRepository jRoomRepository;
  @Autowired JSeatRepository jSeatRepository;
  @Autowired PasswordEncoder passwordEncoder;

  private HttpHeaders clientHeaders;
  private HttpHeaders employeeHeaders;
  private HttpHeaders managerHeaders;
  private UUID clientId;

  @BeforeEach
  void setUp() {
    jReservationRepository.deleteAll();
    jProjectionRepository.deleteAll();
    jSeatRepository.deleteAll();
    jMovieRepository.deleteAll();
    jRoomRepository.deleteAll();
    var client = seedUser("client@cinema.test", UserRole.CLIENT);
    clientId = client.getId();
    clientHeaders = authHeaders(client.getToken());
    employeeHeaders = authHeaders(seedUser("employee@cinema.test", UserRole.EMPLOYEE).getToken());
    managerHeaders = authHeaders(seedUser("manager@cinema.test", UserRole.MANAGER).getToken());
  }

  @Test
  void put_reservations_creates_pending_reservation_for_client() {
    var seeded = seedProjection(2);
    var seatIds = seeded.seats().stream().map(JSeat::getId).toList();

    var response =
        testRestTemplate.exchange(
            RESERVATIONS_URL,
            PUT,
            new HttpEntity<>(
                new CreateReservationRequest(seeded.projection().getId(), seatIds), clientHeaders),
            Reservation.class);

    assertEquals(OK, response.getStatusCode());
    var created = response.getBody();
    assertNotNull(created);
    assertNotNull(created.getId());
    assertNotNull(created.getCreatedAt());
    assertEquals(ReservationStatus.PENDING, created.getStatus());
    assertEquals(clientId, created.getClient().getId());
    assertEquals(seeded.projection().getId(), created.getProjection().getId());
    assertEquals(
        Set.copyOf(seatIds),
        created.getSeats().stream().map(Seat::getId).collect(Collectors.toSet()));

    var byId =
        testRestTemplate.exchange(
            RESERVATIONS_URL + "/{id}",
            GET,
            new HttpEntity<>(clientHeaders),
            Reservation.class,
            created.getId());
    assertEquals(OK, byId.getStatusCode());
    assertEquals(ReservationStatus.PENDING, byId.getBody().getStatus());
  }

  @Test
  void put_reservations_creates_for_employee_and_manager() {
    var seeded = seedProjection(2);

    var employeeResponse =
        testRestTemplate.exchange(
            RESERVATIONS_URL,
            PUT,
            new HttpEntity<>(
                new CreateReservationRequest(
                    seeded.projection().getId(), List.of(seeded.seats().get(0).getId())),
                employeeHeaders),
            Map.class);
    var managerResponse =
        testRestTemplate.exchange(
            RESERVATIONS_URL,
            PUT,
            new HttpEntity<>(
                new CreateReservationRequest(
                    seeded.projection().getId(), List.of(seeded.seats().get(1).getId())),
                managerHeaders),
            Map.class);

    assertEquals(OK, employeeResponse.getStatusCode());
    assertEquals(OK, managerResponse.getStatusCode());
  }

  @Test
  void put_reservations_rejects_seat_already_reserved() {
    var seeded = seedProjection(1);
    var request =
        new CreateReservationRequest(
            seeded.projection().getId(), List.of(seeded.seats().get(0).getId()));

    testRestTemplate.exchange(
        RESERVATIONS_URL, PUT, new HttpEntity<>(request, clientHeaders), Reservation.class);
    var second =
        testRestTemplate.exchange(
            RESERVATIONS_URL, PUT, new HttpEntity<>(request, employeeHeaders), Map.class);

    assertEquals(BAD_REQUEST, second.getStatusCode());
    assertTrue(second.getBody().get("message").toString().contains("already reserved"));
  }

  @Test
  void put_reservations_returns_400_when_projection_missing() {
    var response =
        testRestTemplate.exchange(
            RESERVATIONS_URL,
            PUT,
            new HttpEntity<>(
                new CreateReservationRequest(UUID.randomUUID(), List.of(UUID.randomUUID())),
                clientHeaders),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals("400 BAD_REQUEST", response.getBody().get("type"));
  }

  @Test
  void put_reservations_returns_400_when_seat_missing() {
    var seeded = seedProjection(1);
    var response =
        testRestTemplate.exchange(
            RESERVATIONS_URL,
            PUT,
            new HttpEntity<>(
                new CreateReservationRequest(
                    seeded.projection().getId(), List.of(UUID.randomUUID())),
                clientHeaders),
            Map.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
    assertEquals("Seat(s) not found", response.getBody().get("message"));
  }

  @Test
  void put_reservations_returns_401_when_unauthenticated() {
    var response =
        testRestTemplate.exchange(
            RESERVATIONS_URL, PUT, new HttpEntity<>(Map.of(), jsonHeaders()), Map.class);

    assertEquals(UNAUTHORIZED, response.getStatusCode());
    assertEquals("401 UNAUTHORIZED", response.getBody().get("type"));
  }

  private SeededProjection seedProjection(int seatCount) {
    var movie =
        jMovieRepository.save(
            new JMovie(
                null,
                "Inception",
                Set.of(Genre.ACTION),
                "A thief who steals corporate secrets through dream-sharing",
                Duration.ofHours(2).plusMinutes(28)));
    var room = jRoomRepository.save(new JRoom(null, "1", seatCount));
    var seats =
        IntStream.range(1, seatCount + 1)
            .mapToObj(i -> jSeatRepository.save(new JSeat(null, "A" + i, room)))
            .toList();
    var projection =
        jProjectionRepository.save(new JProjection(null, movie, room, DATETIME, SEAT_PRICE));
    return new SeededProjection(projection, seats);
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

  private record SeededProjection(JProjection projection, List<JSeat> seats) {}
}
