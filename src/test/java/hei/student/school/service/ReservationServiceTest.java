package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import hei.student.school.model.Reservation;
import hei.student.school.model.User;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JReservationRepository;
import hei.student.school.repository.mapper.JReservationMapper;
import hei.student.school.repository.model.JReservation;
import hei.student.school.repository.model.JUser;
import hei.student.school.security.model.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  private static final UUID ID = UUID.randomUUID();

  @Mock JReservationRepository jReservationRepository;
  @Mock JReservationMapper jReservationMapper;
  @InjectMocks ReservationService service;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void findAll_maps_all_reservations_to_domain() {
    var entity = new JReservation();
    var domain = new Reservation();
    when(jReservationRepository.findAll()).thenReturn(List.of(entity));
    when(jReservationMapper.toDomain(entity)).thenReturn(domain);

    var result = service.findAll();

    assertEquals(List.of(domain), result);
  }

  @Test
  void findById_returns_mapped_reservation_for_its_client() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var entity = reservationWithClient(clientId);
    var domain = new Reservation();
    when(jReservationRepository.findById(ID)).thenReturn(Optional.of(entity));
    when(jReservationMapper.toDomain(entity)).thenReturn(domain);

    var result = service.findById(ID);

    assertEquals(domain, result);
  }

  @Test
  void findById_returns_mapped_reservation_for_manager() {
    authenticateAs(UserRole.MANAGER);
    var entity = reservationWithClient(UUID.randomUUID());
    var domain = new Reservation();
    when(jReservationRepository.findById(ID)).thenReturn(Optional.of(entity));
    when(jReservationMapper.toDomain(entity)).thenReturn(domain);

    var result = service.findById(ID);

    assertEquals(domain, result);
  }

  @Test
  void findById_throws_no_such_element_when_reservation_missing() {
    when(jReservationRepository.findById(ID)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> service.findById(ID));
  }

  @Test
  void findById_throws_access_denied_without_authentication() {
    when(jReservationRepository.findById(ID))
        .thenReturn(Optional.of(reservationWithClient(UUID.randomUUID())));

    assertThrows(AccessDeniedException.class, () -> service.findById(ID));
  }

  @Test
  void findById_throws_access_denied_when_principal_is_not_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null));
    when(jReservationRepository.findById(ID))
        .thenReturn(Optional.of(reservationWithClient(UUID.randomUUID())));

    assertThrows(AccessDeniedException.class, () -> service.findById(ID));
  }

  @Test
  void findById_throws_access_denied_for_client_viewing_another_reservation() {
    authenticateAs(UserRole.CLIENT);
    when(jReservationRepository.findById(ID))
        .thenReturn(Optional.of(reservationWithClient(UUID.randomUUID())));

    assertThrows(AccessDeniedException.class, () -> service.findById(ID));
  }

  @Test
  void findById_throws_access_denied_for_client_viewing_reservation_without_client() {
    authenticateAs(UserRole.CLIENT);
    when(jReservationRepository.findById(ID)).thenReturn(Optional.of(new JReservation()));

    assertThrows(AccessDeniedException.class, () -> service.findById(ID));
  }

  private static JReservation reservationWithClient(UUID clientId) {
    var client = new JUser();
    client.setId(clientId);
    var reservation = new JReservation();
    reservation.setId(ID);
    reservation.setClient(client);
    return reservation;
  }

  private static UUID authenticateAs(UserRole role) {
    var id = UUID.randomUUID();
    var user = new User();
    user.setId(id);
    user.setEmail("user@cinema.test");
    user.setRole(role);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(new Principal(user), null));
    return id;
  }
}
