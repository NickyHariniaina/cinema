package hei.student.school.model;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Movie {
  private UUID id;
  private String title;
  private Set<Genre> genres = new HashSet<>();
  private String description;
  private Duration duration;
}
