package hei.student.school.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class JRoomTest {

  @Test
  void all_args_constructor_and_getters() {
    var id = UUID.randomUUID();
    var room = new JRoom(id, "B2", 30);

    assertEquals(id, room.getId());
    assertEquals("B2", room.getNumber());
    assertEquals(30, room.getCapacity());
  }
}
