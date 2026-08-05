package hei.student.school.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class JSeatTest {

  @Test
  void all_args_constructor_and_getters() {
    var id = UUID.randomUUID();
    var room = new JRoom(UUID.randomUUID(), "B2", 30);
    var seat = new JSeat(id, "B12", room);

    assertEquals(id, seat.getId());
    assertEquals("B12", seat.getNumber());
    assertEquals(room, seat.getRoom());
  }
}
