package hei.student.school.repository.mapper;

import hei.student.school.model.Projection;
import hei.student.school.repository.model.JProjection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JProjectionMapper {
  private final JMovieMapper movieMapper;
  private final JRoomMapper roomMapper;

  public Projection toDomain(JProjection entity) {
    var projection = new Projection();
    projection.setId(entity.getId());
    projection.setMovie(entity.getMovie() == null ? null : movieMapper.toDomain(entity.getMovie()));
    projection.setRoom(entity.getRoom() == null ? null : roomMapper.toDomain(entity.getRoom()));
    projection.setDatetime(entity.getDatetime());
    projection.setSeatPrice(entity.getSeatPrice());
    return projection;
  }
}
