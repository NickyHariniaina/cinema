package hei.student.school.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hei.student.school.repository.model.JRoom;
import hei.student.school.repository.model.JSeat;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JSeatMapperTest {

  private final JRoomMapper roomMapper = new JRoomMapper();
  private final JSeatMapper mapper = new JSeatMapper(roomMapper);

  @Test
  void toDomain_maps_all_fields() {
    var room = new JRoom(UUID.randomUUID(), "B2", 30);
    var entity = new JSeat(UUID.randomUUID(), "B12", room);

    var seat = mapper.toDomain(entity);

    assertEquals(entity.getId(), seat.getId());
    assertEquals("B12", seat.getNumber());
    assertEquals("B2", seat.getRoom().getNumber());
  }

  @Test
  void toDomain_handles_null_room() {
    var entity = new JSeat(UUID.randomUUID(), "B12", null);

    var seat = mapper.toDomain(entity);

    assertNull(seat.getRoom());
  }
}
