package hei.student.school.service.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.school.model.AuthPayload;
import hei.student.school.repository.JUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserPayloadValidatorTest {

  @Mock JUserRepository jUserRepository;
  @InjectMocks RegisterUserPayloadValidator validator;

  @Test
  void accept_rejects_blank_email_and_password() {
    var exception =
        assertThrows(
            IllegalArgumentException.class, () -> validator.accept(new AuthPayload(null, null)));

    assertEquals("Email is mandatory. Password is mandatory", exception.getMessage());
    verify(jUserRepository, never()).existsByEmail(anyString());
  }

  @Test
  void accept_rejects_already_taken_email() {
    when(jUserRepository.existsByEmail("client@cinema.test")).thenReturn(true);

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new AuthPayload("client@cinema.test", "password123")));

    assertEquals("Email(client@cinema.test) is already taken", exception.getMessage());
  }

  @Test
  void accept_rejects_password_shorter_than_eight_characters() {
    when(jUserRepository.existsByEmail("client@cinema.test")).thenReturn(false);

    var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.accept(new AuthPayload("client@cinema.test", "short")));

    assertEquals("Password must be at least 8 characters", exception.getMessage());
  }

  @Test
  void accept_passes_valid_payload() {
    when(jUserRepository.existsByEmail("client@cinema.test")).thenReturn(false);

    validator.accept(new AuthPayload("client@cinema.test", "password123"));
  }
}
