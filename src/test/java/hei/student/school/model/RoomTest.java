package hei.student.school.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoomTest {

  @Test
  void sets_and_gets_fields() {
    var id = UUID.randomUUID();
    var room = new Room();
    room.setId(id);
    room.setNumber("A1");
    room.setCapacity(50);

    assertEquals(id, room.getId());
    assertEquals("A1", room.getNumber());
    assertEquals(50, room.getCapacity());
  }
}
