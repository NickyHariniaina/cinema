package hei.student.school.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SeatTest {

  @Test
  void sets_and_gets_fields() {
    var id = UUID.randomUUID();
    var room = new Room();
    room.setNumber("A1");
    var seat = new Seat();
    seat.setId(id);
    seat.setNumber("A12");
    seat.setRoom(room);

    assertEquals(id, seat.getId());
    assertEquals("A12", seat.getNumber());
    assertEquals(room, seat.getRoom());
  }
}
