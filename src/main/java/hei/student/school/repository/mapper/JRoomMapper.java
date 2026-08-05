package hei.student.school.repository.mapper;

import hei.student.school.model.Room;
import hei.student.school.repository.model.JRoom;
import org.springframework.stereotype.Component;

@Component
public class JRoomMapper {
  public Room toDomain(JRoom entity) {
    var room = new Room();
    room.setId(entity.getId());
    room.setNumber(entity.getNumber());
    room.setCapacity(entity.getCapacity());
    return room;
  }
}
