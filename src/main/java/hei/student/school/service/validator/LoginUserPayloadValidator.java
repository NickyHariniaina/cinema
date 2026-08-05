package hei.student.school.service.validator;

import hei.student.school.model.AuthPayload;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class LoginUserPayloadValidator implements Consumer<AuthPayload> {
  @Override
  public void accept(AuthPayload payload) {
    if (payload == null) {
      throw new IllegalArgumentException("payload is null");
    }

    var email = payload.email();
    var password = payload.password();

    var sj = new StringJoiner(". ");

    if (email == null) {
      sj.add("Email is required");
    }

    if (password == null) {
      sj.add("Password is required");
    }

    if (sj.length() > 0) {
      throw new IllegalArgumentException(sj.toString());
    }
  }
}
