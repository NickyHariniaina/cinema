package hei.student.school.service.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hei.student.school.model.CreateReservationRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateReservationValidatorTest {

  private final CreateReservationValidator validator = new CreateReservationValidator();

  @Test
  void accept_does_not_throw_when_all_fields_present() {
    assertDoesNotThrow(() -> validator.accept(validRequest()));
  }

  @Test
  void accept_collects_all_missing_fields() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new CreateReservationRequest(null, null)));

    assertEquals(
        "ProjectionId is mandatory. At least one seat is required", exception.getMessage());
  }

  @Test
  void accept_rejects_empty_seat_ids() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new CreateReservationRequest(UUID.randomUUID(), List.of())));

    assertEquals("At least one seat is required", exception.getMessage());
  }

  @Test
  void accept_rejects_missing_projection_id() {
    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new CreateReservationRequest(null, List.of(UUID.randomUUID()))));

    assertEquals("ProjectionId is mandatory", exception.getMessage());
  }

  private static CreateReservationRequest validRequest() {
    return new CreateReservationRequest(
        UUID.randomUUID(), List.of(UUID.randomUUID(), UUID.randomUUID()));
  }
}
