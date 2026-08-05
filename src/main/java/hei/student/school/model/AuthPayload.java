package hei.student.school.model;

public record AuthPayload(String email, String password) {
  public User toNewUser() {
    var user = new User();
    user.setEmail(email);
    user.setPassword(password);
    user.setRole(UserRole.CLIENT);
    return user;
  }
}
