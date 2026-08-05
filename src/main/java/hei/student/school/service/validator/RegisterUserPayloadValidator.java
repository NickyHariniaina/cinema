package hei.student.school.service.validator;

import hei.student.school.model.AuthPayload;
import hei.student.school.repository.JUserRepository;
import java.util.StringJoiner;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class RegisterUserPayloadValidator implements Consumer<AuthPayload> {
  private JUserRepository jUserRepository;

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  @Override
  public void accept(AuthPayload payload) {
    var sj = new StringJoiner(". ");

    if (isBlank(payload.email())) {
      sj.add("Email is mandatory");
    } else if (jUserRepository.existsByEmail(payload.email())) {
      sj.add("Email(" + payload.email() + ") is already taken");
    }

    if (isBlank(payload.password())) {
      sj.add("Password is mandatory");
    } else if (payload.password().length() < 8) {
      sj.add("Password must be at least 8 characters");
    }

    if (sj.length() > 0) {
      throw new IllegalArgumentException(sj.toString());
    }
  }
}
