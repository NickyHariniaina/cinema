package hei.student.school.model;

import lombok.Getter;

public class UserWithToken extends User {
  @Getter
  private final String token;

  public UserWithToken(String token) {
    this.token = token;
  }

  public static UserWithToken from(User user, String token) {
    var dto = new UserWithToken(token);
    dto.setId(user.getId());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setBirthdate(user.getBirthdate());
    dto.setEmail(user.getEmail());
    dto.setPassword(user.getPassword());
    dto.setPhone(user.getPhone());
    dto.setRole(user.getRole());
    return dto;
  }
}
