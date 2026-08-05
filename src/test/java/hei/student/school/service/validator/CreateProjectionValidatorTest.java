package hei.student.school.service.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hei.student.school.model.ProjectionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateProjectionValidatorTest {

  private final CreateProjectionValidator validator = new CreateProjectionValidator();

  @Test
  void accept_does_not_throw_when_all_fields_present() {
    assertDoesNotThrow(() -> validator.accept(validRequest()));
  }

  @Test
  void accept_collects_all_missing_fields() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new ProjectionRequest(null, null, null, null)));

    assertEquals(
        "MovieId is mandatory. RoomId is mandatory. Datetime is mandatory. SeatPrice is mandatory",
        exception.getMessage());
  }

  @Test
  void accept_rejects_missing_movie_id() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.accept(
                    new ProjectionRequest(null, UUID.randomUUID(), Instant.now(), BigDecimal.TEN)));

    assertEquals("MovieId is mandatory", exception.getMessage());
  }

  @Test
  void accept_rejects_missing_room_id() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.accept(
                    new ProjectionRequest(UUID.randomUUID(), null, Instant.now(), BigDecimal.TEN)));

    assertEquals("RoomId is mandatory", exception.getMessage());
  }

  @Test
  void accept_rejects_missing_datetime() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.accept(
                    new ProjectionRequest(
                        UUID.randomUUID(), UUID.randomUUID(), null, BigDecimal.TEN)));

    assertEquals("Datetime is mandatory", exception.getMessage());
  }

  @Test
  void accept_rejects_missing_seat_price() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                validator.accept(
                    new ProjectionRequest(
                        UUID.randomUUID(), UUID.randomUUID(), Instant.now(), null)));

    assertEquals("SeatPrice is mandatory", exception.getMessage());
  }

  private static ProjectionRequest validRequest() {
    return new ProjectionRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        Instant.parse("2026-08-06T20:00:00Z"),
        BigDecimal.TEN);
  }
}
