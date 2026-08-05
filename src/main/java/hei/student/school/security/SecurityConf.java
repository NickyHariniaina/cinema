package hei.student.school.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import hei.student.school.security.exception.RestAccessDeniedHandler;
import hei.student.school.security.exception.RestAuthenticationEntryPoint;
import hei.student.school.security.filter.BearerAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConf {
  private final RestAuthenticationEntryPoint entryPoint;
  private final RestAccessDeniedHandler accessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, BearerAuthFilter bearerAuthFilter)
      throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
        .exceptionHandling(
            e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/register", "/login")
                    .anonymous()
                    .requestMatchers("/movies", "/movies/**")
                    .hasRole("MANAGER")
                    .requestMatchers(PUT, "/projections", "/projections/**")
                    .hasRole("MANAGER")
                    .requestMatchers(GET, "/reservations")
                    .hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers(GET, "/reservations/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(bearerAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
