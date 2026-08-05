package hei.student.school.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.school.model.Genre;
import hei.student.school.repository.model.JMovie;
import hei.student.school.repository.model.JProjection;
import hei.student.school.repository.model.JRoom;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JProjectionMapperTest {

  private final JMovieMapper movieMapper = new JMovieMapper();
  private final JRoomMapper roomMapper = new JRoomMapper();
  private final JProjectionMapper mapper = new JProjectionMapper(movieMapper, roomMapper);

  @Test
  void toDomain_maps_all_fields() {
    var movie =
        new JMovie(
            UUID.randomUUID(),
            "Inception",
            Set.of(Genre.ACTION),
            "Dream heist",
            Duration.ofHours(2));
    var room = new JRoom(UUID.randomUUID(), "B2", 30);
    var datetime = Instant.parse("2026-08-05T18:00:00Z");
    var entity = new JProjection(UUID.randomUUID(), movie, room, datetime, new BigDecimal("12.50"));

    var projection = mapper.toDomain(entity);

    assertEquals(entity.getId(), projection.getId());
    assertEquals("Inception", projection.getMovie().getTitle());
    assertEquals(Set.of(Genre.ACTION), projection.getMovie().getGenres());
    assertEquals("B2", projection.getRoom().getNumber());
    assertEquals(datetime, projection.getDatetime());
    assertEquals(new BigDecimal("12.50"), projection.getSeatPrice());
  }

  @Test
  void toDomain_handles_null_movie_and_room() {
    var entity = new JProjection(UUID.randomUUID(), null, null, null, null);

    var projection = mapper.toDomain(entity);

    assertNull(projection.getMovie());
    assertNull(projection.getRoom());
  }
}
