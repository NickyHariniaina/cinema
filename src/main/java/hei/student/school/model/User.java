package hei.student.school.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {
  private UUID id;
  private String firstName;
  private String lastName;
  private LocalDate birthdate;
  private String email;
  private String password;
  private String phone;
  private UserRole role;
}
