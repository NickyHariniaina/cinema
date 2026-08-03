package hei.student.school.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projection")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JProjection {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "movie_id")
  private JMovie movie;

  @ManyToOne
  @JoinColumn(name = "room_id")
  private JRoom room;

  private Instant datetime;
  private BigDecimal seatPrice;
}
