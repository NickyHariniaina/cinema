package hei.student.school.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserWithTokenTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void deserializes_login_response() throws Exception {
    var json =
        """
        {
          "id": "11111111-1111-1111-1111-111111111111",
          "firstName": null,
          "lastName": null,
          "birthdate": null,
          "email": "manager@cinema.test",
          "phone": null,
          "role": "MANAGER",
          "token": "a-login-token"
        }
        """;

    var user = MAPPER.readValue(json, UserWithToken.class);

    assertEquals("a-login-token", user.getToken());
    assertEquals("manager@cinema.test", user.getEmail());
    assertEquals(UserRole.MANAGER, user.getRole());
  }
}
