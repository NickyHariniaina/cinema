package hei.student.school.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.school.model.Genre;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JProjectionTest {

  @Test
  void all_args_constructor_and_getters() {
    var id = UUID.randomUUID();
    var movie =
        new JMovie(
            UUID.randomUUID(),
            "Inception",
            Set.of(Genre.ACTION),
            "Dream heist",
            Duration.ofHours(2));
    var room = new JRoom(UUID.randomUUID(), "B2", 30);
    var datetime = Instant.parse("2026-08-05T18:00:00Z");
    var price = new BigDecimal("12.50");

    var projection = new JProjection(id, movie, room, datetime, price);

    assertEquals(id, projection.getId());
    assertEquals(movie, projection.getMovie());
    assertEquals(room, projection.getRoom());
    assertEquals(datetime, projection.getDatetime());
    assertEquals(price, projection.getSeatPrice());
  }
}
