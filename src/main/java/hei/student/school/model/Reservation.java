package hei.student.school.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Reservation {
  private UUID id;
  private Instant createdAt;
  private ReservationStatus status;
  private User client;
  private Projection projection;
  private Set<Seat> seats = new HashSet<>();
}
