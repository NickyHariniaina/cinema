package hei.student.school.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectionTest {

  @Test
  void sets_and_gets_fields() {
    var id = UUID.randomUUID();
    var movie = new Movie();
    movie.setTitle("Inception");
    var room = new Room();
    room.setNumber("A1");
    var datetime = Instant.parse("2026-08-05T18:00:00Z");
    var price = new BigDecimal("12.50");

    var projection = new Projection();
    projection.setId(id);
    projection.setMovie(movie);
    projection.setRoom(room);
    projection.setDatetime(datetime);
    projection.setSeatPrice(price);

    assertEquals(id, projection.getId());
    assertEquals(movie, projection.getMovie());
    assertEquals(room, projection.getRoom());
    assertEquals(datetime, projection.getDatetime());
    assertEquals(price, projection.getSeatPrice());
  }
}
