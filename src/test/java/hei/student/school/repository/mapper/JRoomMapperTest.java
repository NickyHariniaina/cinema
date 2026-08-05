package hei.student.school.repository.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hei.student.school.repository.model.JRoom;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JRoomMapperTest {

  private final JRoomMapper mapper = new JRoomMapper();

  @Test
  void toDomain_maps_all_fields() {
    var id = UUID.randomUUID();
    var entity = new JRoom(id, "B2", 30);

    var room = mapper.toDomain(entity);

    assertEquals(id, room.getId());
    assertEquals("B2", room.getNumber());
    assertEquals(30, room.getCapacity());
  }
}
