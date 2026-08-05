package hei.student.school.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hei.student.school.model.User;
import hei.student.school.model.UserRole;
import hei.student.school.security.model.Principal;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "test-secret-key-with-more-than-32-bytes-for-hs256";
  private static final long EXPIRATION_MS = 3600000;

  private final JwtService jwtService = new JwtService(SECRET, EXPIRATION_MS);

  @Test
  void generate_then_extractUsername_roundtrip() {
    var token = jwtService.generate(principal("client@cinema.test", UserRole.CLIENT));

    assertEquals("client@cinema.test", jwtService.extractUsername(token));
  }

  @Test
  void isValid_returns_true_for_valid_token() {
    var token = jwtService.generate(principal("client@cinema.test", UserRole.CLIENT));

    assertTrue(jwtService.isValid(token, "client@cinema.test"));
  }

  @Test
  void isValid_returns_false_for_wrong_username() {
    var token = jwtService.generate(principal("client@cinema.test", UserRole.CLIENT));

    assertFalse(jwtService.isValid(token, "manager@cinema.test"));
  }

  @Test
  void isValid_returns_false_for_malformed_token() {
    assertFalse(jwtService.isValid("not-a-jwt-token", "client@cinema.test"));
  }

  @Test
  void isValid_returns_false_for_token_signed_with_different_key() {
    var token = jwtService.generate(principal("client@cinema.test", UserRole.CLIENT));
    var otherService =
        new JwtService("another-secret-key-with-more-than-32-bytes-for-hs256", EXPIRATION_MS);

    assertFalse(otherService.isValid(token, "client@cinema.test"));
  }

  @Test
  void isValid_returns_false_for_expired_token() {
    var expiredService = new JwtService(SECRET, -1000);
    var token = expiredService.generate(principal("client@cinema.test", UserRole.CLIENT));

    assertFalse(expiredService.isValid(token, "client@cinema.test"));
  }

  @Test
  void extractUsername_throws_for_malformed_token() {
    assertThrows(JwtException.class, () -> jwtService.extractUsername("not-a-jwt-token"));
  }

  private static Principal principal(String email, UserRole role) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setRole(role);
    return new Principal(user);
  }
}
