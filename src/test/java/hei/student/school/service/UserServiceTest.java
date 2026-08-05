package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.school.model.AuthPayload;
import hei.student.school.model.User;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JUserMapper;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.model.JUser;
import hei.student.school.security.jwt.JwtService;
import hei.student.school.security.model.Principal;
import hei.student.school.service.validator.LoginUserPayloadValidator;
import hei.student.school.service.validator.RegisterUserPayloadValidator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock JUserRepository jUserRepository;
  @Mock JUserMapper jUserMapper;
  @Mock RegisterUserPayloadValidator registerUserPayloadValidator;
  @Mock LoginUserPayloadValidator loginUserPayloadValidator;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;
  @InjectMocks UserService userService;

  @Test
  void register_encodes_password_and_returns_token() {
    var payload = new AuthPayload("client@cinema.test", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    var toSave =
        new JUser(
            null,
            null,
            null,
            null,
            "client@cinema.test",
            "encoded-password",
            null,
            UserRole.CLIENT);
    when(jUserMapper.toEntity(any(User.class))).thenReturn(toSave);
    var saved =
        new JUser(
            UUID.randomUUID(),
            "Client",
            "Cinema",
            LocalDate.of(2000, 1, 1),
            "client@cinema.test",
            "encoded-password",
            "0123456789",
            UserRole.CLIENT);
    when(jUserRepository.save(toSave)).thenReturn(saved);
    when(jUserMapper.toDomain(saved)).thenReturn(domainUser(saved));
    when(jwtService.generate(any(Principal.class))).thenReturn("jwt-token");

    var result = userService.register(payload);

    assertEquals("jwt-token", result.getToken());
    assertEquals("client@cinema.test", result.getEmail());
    verify(passwordEncoder).encode("password123");
    var captor = ArgumentCaptor.forClass(User.class);
    verify(jUserMapper).toEntity(captor.capture());
    assertEquals("encoded-password", captor.getValue().getPassword());
    verify(registerUserPayloadValidator).accept(payload);
  }

  @Test
  void register_propagates_validation_error() {
    doThrow(new IllegalArgumentException("Email is mandatory"))
        .when(registerUserPayloadValidator)
        .accept(any(AuthPayload.class));

    assertThrows(
        IllegalArgumentException.class,
        () -> userService.register(new AuthPayload("client@cinema.test", "password123")));

    verify(jUserRepository, never()).save(any());
  }

  @Test
  void login_returns_token_on_valid_credentials() {
    var email = "client@cinema.test";
    var entity =
        new JUser(
            UUID.randomUUID(),
            "Client",
            "Cinema",
            null,
            email,
            "encoded",
            "0123456789",
            UserRole.CLIENT);
    when(jUserRepository.findByEmail(email)).thenReturn(Optional.of(entity));
    when(jUserMapper.toDomain(entity)).thenReturn(domainUser(entity));
    when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
    when(jwtService.generate(any(Principal.class))).thenReturn("jwt-token");

    var result = userService.login(new AuthPayload(email, "password123"));

    assertEquals("jwt-token", result.getToken());
    verify(loginUserPayloadValidator).accept(any(AuthPayload.class));
  }

  @Test
  void login_throws_bad_credentials_when_user_not_found() {
    when(jUserRepository.findByEmail("ghost@cinema.test")).thenReturn(Optional.empty());

    assertThrows(
        BadCredentialsException.class,
        () -> userService.login(new AuthPayload("ghost@cinema.test", "password123")));

    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  void login_throws_bad_credentials_when_password_mismatch() {
    var email = "client@cinema.test";
    var entity =
        new JUser(
            UUID.randomUUID(),
            "Client",
            "Cinema",
            null,
            email,
            "encoded",
            "0123456789",
            UserRole.CLIENT);
    when(jUserRepository.findByEmail(email)).thenReturn(Optional.of(entity));
    when(jUserMapper.toDomain(entity)).thenReturn(domainUser(entity));
    when(passwordEncoder.matches("wrong-password", "encoded")).thenReturn(false);

    assertThrows(
        BadCredentialsException.class,
        () -> userService.login(new AuthPayload(email, "wrong-password")));

    verify(jwtService, never()).generate(any());
  }

  @Test
  void loadUserByUsername_returns_principal() {
    var email = "client@cinema.test";
    var entity =
        new JUser(
            UUID.randomUUID(),
            "Client",
            "Cinema",
            null,
            email,
            "encoded",
            "0123456789",
            UserRole.CLIENT);
    when(jUserRepository.findByEmail(email)).thenReturn(Optional.of(entity));
    when(jUserMapper.toDomain(entity)).thenReturn(domainUser(entity));

    var principal = userService.loadUserByUsername(email);

    assertEquals(email, principal.getUsername());
    assertEquals(UserRole.CLIENT, principal.user().getRole());
    assertTrue(principal.isEnabled());
  }

  private static User domainUser(JUser entity) {
    var user = new User();
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
}
