package hei.student.school.service;

import hei.student.school.model.AuthPayload;
import hei.student.school.model.User;
import hei.student.school.model.UserWithToken;
import hei.student.school.repository.JUserMapper;
import hei.student.school.repository.JUserRepository;
import hei.student.school.security.jwt.JwtService;
import hei.student.school.security.model.Principal;
import hei.student.school.service.validator.LoginUserPayloadValidator;
import hei.student.school.service.validator.RegisterUserPayloadValidator;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
  private final JUserRepository jUserRepository;
  private final JUserMapper jUserMapper;
  private final RegisterUserPayloadValidator registerUserPayloadValidator;
  private final LoginUserPayloadValidator loginUserPayloadValidator;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  private User getByEmail(String email) {
    return jUserRepository.findByEmail(email)
        .map(jUserMapper::toDomain)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
  }

  public UserWithToken register(AuthPayload payload) {
    registerUserPayloadValidator.accept(payload);

    var toRegister = payload.toNewUser();
    var encodedPassword = passwordEncoder.encode(toRegister.getPassword());
    toRegister.setPassword(encodedPassword);

    var saved = jUserRepository.save(jUserMapper.toEntity(toRegister));
    var domain = jUserMapper.toDomain(saved);
    var token = jwtService.generate(new Principal(domain));
    return UserWithToken.from(domain, token);
  }

  public UserWithToken login(AuthPayload payload) {
    loginUserPayloadValidator.accept(payload);

    Principal principal;
    try {
      principal = loadUserByUsername(payload.email());
    } catch (NoSuchElementException e) {
      throw new BadCredentialsException("Invalid email or password");
    }

    if (!passwordEncoder.matches(payload.password(), principal.getPassword())) {
      throw new BadCredentialsException("Invalid email or password");
    }

    var token = jwtService.generate(principal);
    return UserWithToken.from(principal.user(), token);
  }

  @Override
  public Principal loadUserByUsername(String email) throws UsernameNotFoundException {
    return new Principal(getByEmail(email));
  }
}
