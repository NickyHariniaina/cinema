package hei.student.school.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hei.student.school.model.Reservation;
import hei.student.school.model.ReservationStatus;
import hei.student.school.security.jwt.JwtService;
import hei.student.school.service.ReservationService;
import hei.student.school.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

  private static final String RESERVATIONS_URL = "/reservations";

  @Autowired MockMvc mockMvc;
  @MockBean ReservationService reservationService;
  @MockBean UserService userService;
  @MockBean JwtService jwtService;

  @Test
  void get_reservations_returns_200_with_all_reservations() throws Exception {
    when(reservationService.findAll()).thenReturn(List.of(reservation()));

    mockMvc
        .perform(get(RESERVATIONS_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  void get_reservation_by_id_returns_200() throws Exception {
    var id = UUID.randomUUID();
    when(reservationService.findById(id)).thenReturn(reservation());

    mockMvc
        .perform(get(RESERVATIONS_URL + "/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void get_reservation_by_id_returns_404_when_not_found() throws Exception {
    when(reservationService.findById(any(UUID.class)))
        .thenThrow(
            new NoSuchElementException("Reservation with id " + UUID.randomUUID() + " not found"));

    mockMvc
        .perform(get(RESERVATIONS_URL + "/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("404 NOT_FOUND"));
  }

  @Test
  void get_reservation_by_id_returns_403_when_access_denied() throws Exception {
    when(reservationService.findById(any(UUID.class)))
        .thenThrow(new AccessDeniedException("A CLIENT can only view its own reservations"));

    mockMvc
        .perform(get(RESERVATIONS_URL + "/{id}", UUID.randomUUID()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("403 FORBIDDEN"));
  }

  private static Reservation reservation() {
    var reservation = new Reservation();
    reservation.setId(UUID.randomUUID());
    reservation.setCreatedAt(Instant.parse("2026-08-05T10:00:00Z"));
    reservation.setStatus(ReservationStatus.PENDING);
    return reservation;
  }
}
