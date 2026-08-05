package hei.student.school.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.student.school.model.AuthPayload;
import hei.student.school.model.User;
import hei.student.school.model.UserRole;
import hei.student.school.model.UserWithToken;
import hei.student.school.service.UserService;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  private static final String REGISTER_URL = "/register";
  private static final String LOGIN_URL = "/login";
  private static final String REGISTER_JSON =
      "{\"email\":\"client@cinema.test\",\"password\":\"password123\"}";

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean UserService userService;

  @Test
  void register_returns_201_with_token() throws Exception {
    when(userService.register(any(AuthPayload.class)))
        .thenReturn(UserWithToken.from(newUser(), "jwt-token"));

    mockMvc
        .perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(REGISTER_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.email").value("client@cinema.test"));
  }

  @Test
  void register_returns_400_when_payload_invalid() throws Exception {
    when(userService.register(any(AuthPayload.class)))
        .thenThrow(
            new IllegalArgumentException(
                "Email is mandatory. Password must be at least 8 characters"));

    mockMvc
        .perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("400 BAD_REQUEST"))
        .andExpect(
            jsonPath("$.message")
                .value("Email is mandatory. Password must be at least 8 characters"));
  }

  @Test
  void login_returns_200_with_token() throws Exception {
    when(userService.login(any(AuthPayload.class))).thenReturn(new UserWithToken("login-token"));

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("login-token"));
  }

  @Test
  void login_returns_404_when_user_not_found() throws Exception {
    when(userService.login(any(AuthPayload.class)))
        .thenThrow(new NoSuchElementException("User not found"));

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("404 NOT_FOUND"));
  }

  @Test
  void login_returns_401_when_bad_credentials() throws Exception {
    when(userService.login(any(AuthPayload.class)))
        .thenThrow(new BadCredentialsException("Invalid email or password"));

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("401 UNAUTHORIZED"));
  }

  @Test
  void login_returns_403_when_access_denied() throws Exception {
    when(userService.login(any(AuthPayload.class)))
        .thenThrow(new AccessDeniedException("Access denied"));

    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("403 FORBIDDEN"));
  }

  private static User newUser() {
    var user = new User();
    user.setEmail("client@cinema.test");
    user.setRole(UserRole.CLIENT);
    return user;
  }
}
