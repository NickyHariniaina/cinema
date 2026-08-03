package hei.student.school.model;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Room {
  private UUID id;
  private String number;
  private int capacity;
}
