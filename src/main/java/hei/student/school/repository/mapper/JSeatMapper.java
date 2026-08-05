package hei.student.school.repository.mapper;

import hei.student.school.model.Seat;
import hei.student.school.repository.model.JSeat;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JSeatMapper {
  private final JRoomMapper roomMapper;

  public Seat toDomain(JSeat entity) {
    var seat = new Seat();
    seat.setId(entity.getId());
    seat.setNumber(entity.getNumber());
    seat.setRoom(entity.getRoom() == null ? null : roomMapper.toDomain(entity.getRoom()));
    return seat;
  }
}
