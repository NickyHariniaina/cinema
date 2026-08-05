package hei.student.school.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.school.model.Genre;
import hei.student.school.model.ReservationStatus;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JUserMapper;
import hei.student.school.repository.model.JMovie;
import hei.student.school.repository.model.JProjection;
import hei.student.school.repository.model.JReservation;
import hei.student.school.repository.model.JRoom;
import hei.student.school.repository.model.JSeat;
import hei.student.school.repository.model.JUser;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JReservationMapperTest {

  private final JUserMapper userMapper = new JUserMapper();
  private final JMovieMapper movieMapper = new JMovieMapper();
  private final JRoomMapper roomMapper = new JRoomMapper();
  private final JSeatMapper seatMapper = new JSeatMapper(roomMapper);
  private final JProjectionMapper projectionMapper = new JProjectionMapper(movieMapper, roomMapper);
  private final JReservationMapper mapper =
      new JReservationMapper(userMapper, projectionMapper, seatMapper);

  @Test
  void toDomain_maps_all_fields() {
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
    var id = UUID.randomUUID();
    var createdAt = Instant.parse("2026-08-05T10:00:00Z");
    var entity =
        new JReservation(
            id, createdAt, ReservationStatus.PENDING, client, projection, Set.of(seat));

    var reservation = mapper.toDomain(entity);

    assertEquals(id, reservation.getId());
    assertEquals(createdAt, reservation.getCreatedAt());
    assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    assertEquals("client@cinema.test", reservation.getClient().getEmail());
    assertEquals("Inception", reservation.getProjection().getMovie().getTitle());
    assertEquals("B2", reservation.getSeats().iterator().next().getRoom().getNumber());
  }

  @Test
  void toDomain_handles_null_client_and_projection() {
    var entity = new JReservation(UUID.randomUUID(), null, null, null, null, Set.of());

    var reservation = mapper.toDomain(entity);

    assertNull(reservation.getClient());
    assertNull(reservation.getProjection());
    assertTrue(reservation.getSeats().isEmpty());
  }
}
