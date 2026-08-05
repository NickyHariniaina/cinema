package hei.student.school.security.filter;

import hei.student.school.security.jwt.JwtService;
import hei.student.school.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class BearerAuthFilter extends OncePerRequestFilter {
  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var header = request.getHeader(HEADER);

    if (header != null && header.startsWith(PREFIX)) {
      var token = header.substring(PREFIX.length());
      var username = safeExtractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          var principal = userService.loadUserByUsername(username);

          if (jwtService.isValid(token, username)) {
            var authentication =
                new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        } catch (Exception ignored) {
          // leave unauthenticated; the entry point produces the 401
        }
      }
    }

    chain.doFilter(request, response);
  }

  private String safeExtractUsername(String token) {
    try {
      return jwtService.extractUsername(token);
    } catch (Exception e) {
      return null;
    }
  }
}
