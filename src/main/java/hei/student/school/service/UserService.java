package hei.student.school.service;

import hei.student.school.repository.JUserMapper;
import hei.student.school.repository.JUserRepository;
import hei.student.school.security.model.Principal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
  private final JUserRepository jUserRepository;
  private final JUserMapper jUserMapper;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return jUserRepository
        .findByEmail(email)
        .map(jUserMapper::toDomain)
        .map(Principal::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }
}
