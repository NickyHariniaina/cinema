package hei.student.school.repository.mapper;

import hei.student.school.model.Reservation;
import hei.student.school.repository.JUserMapper;
import hei.student.school.repository.model.JReservation;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JReservationMapper {
  private final JUserMapper userMapper;
  private final JProjectionMapper projectionMapper;
  private final JSeatMapper seatMapper;

  public Reservation toDomain(JReservation entity) {
    var reservation = new Reservation();
    reservation.setId(entity.getId());
    reservation.setCreatedAt(entity.getCreatedAt());
    reservation.setStatus(entity.getStatus());
    reservation.setClient(entity.getClient() == null ? null : userMapper.toDomain(entity.getClient()));
    reservation.setProjection(
        entity.getProjection() == null ? null : projectionMapper.toDomain(entity.getProjection()));
    reservation.setSeats(
        entity.getSeats().stream().map(seatMapper::toDomain).collect(Collectors.toSet()));
    return reservation;
  }
}
