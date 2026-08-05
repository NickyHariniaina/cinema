package hei.student.school.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationTest {

  @Test
  void seats_default_to_empty_set() {
    var reservation = new Reservation();

    assertTrue(reservation.getSeats().isEmpty());
  }

  @Test
  void sets_and_gets_fields() {
    var id = UUID.randomUUID();
    var client = new User();
    client.setEmail("client@cinema.test");
    var projection = new Projection();
    projection.setDatetime(Instant.parse("2026-08-05T18:00:00Z"));
    var seat = new Seat();
    seat.setNumber("A12");

    var reservation = new Reservation();
    reservation.setId(id);
    reservation.setCreatedAt(Instant.parse("2026-08-05T10:00:00Z"));
    reservation.setStatus(ReservationStatus.PENDING);
    reservation.setClient(client);
    reservation.setProjection(projection);
    reservation.setSeats(Set.of(seat));

    assertEquals(id, reservation.getId());
    assertEquals(Instant.parse("2026-08-05T10:00:00Z"), reservation.getCreatedAt());
    assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    assertEquals("CANCELED", ReservationStatus.CANCELED.name());
    assertEquals("SUCCESS", ReservationStatus.SUCCESS.name());
    assertEquals(client, reservation.getClient());
    assertEquals(projection, reservation.getProjection());
    assertEquals(Set.of(seat), reservation.getSeats());
  }
}
