package hei.student.school.endpoint.rest.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InternalToRestExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException e) {
    return new ResponseEntity<>(toRest(e, NOT_FOUND), NOT_FOUND);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    return new ResponseEntity<>(toRest(e, BAD_REQUEST), BAD_REQUEST);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception e) {
    return new ResponseEntity<>(toRest(e, UNAUTHORIZED), UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
    return new ResponseEntity<>(toRest(e, FORBIDDEN), FORBIDDEN);
  }

  private static Map<String, String> toRest(Exception exception, HttpStatus status) {
    return Map.of(
        "message", exception.getMessage(),
        "type", status.toString()
    );
  }
}
