package hei.student.school.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Projection {
  private UUID id;
  private Movie movie;
  private Room room;
  private Instant datetime;
  private BigDecimal seatPrice;
}
