package hei.student.school.repository;

import hei.student.school.model.User;
import hei.student.school.repository.model.JUser;
import org.springframework.stereotype.Component;

@Component
public class JUserMapper {
  public User toDomain(JUser entity) {
    User user = new User();
    user.setId(entity.getId());
    user.setFirstName(entity.getFirstName());
    user.setLastName(entity.getLastName());
    user.setBirthdate(entity.getBirthdate());
    user.setEmail(entity.getEmail());
    user.setPassword(entity.getPassword());
    user.setPhone(entity.getPhone());
    user.setRole(entity.getRole());
    return user;
  }

  public JUser toEntity(User user) {
    return new JUser(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getBirthdate(),
        user.getEmail(),
        user.getPassword(),
        user.getPhone(),
        user.getRole());
  }
}
