package hei.student.school.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.school.model.Genre;
import hei.student.school.model.ReservationStatus;
import hei.student.school.model.UserRole;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JReservationTest {

  @Test
  void seats_default_to_empty_set() {
    var reservation = new JReservation();

    assertTrue(reservation.getSeats().isEmpty());
  }

  @Test
  void all_args_constructor_and_getters() {
    var id = UUID.randomUUID();
    var client =
        new JUser(
            UUID.randomUUID(),
            "Client",
            "Cinema",
            LocalDate.of(2000, 1, 1),
            "client@cinema.test",
            "encoded",
            "0123456789",
            UserRole.CLIENT);
    var movie =
        new JMovie(
            UUID.randomUUID(),
            "Inception",
            Set.of(Genre.ACTION),
            "Dream heist",
            Duration.ofHours(2));
    var room = new JRoom(UUID.randomUUID(), "B2", 30);
    var projection =
        new JProjection(
            UUID.randomUUID(),
            movie,
            room,
            Instant.parse("2026-08-05T18:00:00Z"),
            new BigDecimal("12.50"));
    var seat = new JSeat(UUID.randomUUID(), "B12", room);
    var createdAt = Instant.parse("2026-08-05T10:00:00Z");

    var reservation =
        new JReservation(
            id, createdAt, ReservationStatus.PENDING, client, projection, Set.of(seat));

    assertEquals(id, reservation.getId());
    assertEquals(createdAt, reservation.getCreatedAt());
    assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    assertEquals(client, reservation.getClient());
    assertEquals(projection, reservation.getProjection());
    assertEquals(Set.of(seat), reservation.getSeats());
  }
}
