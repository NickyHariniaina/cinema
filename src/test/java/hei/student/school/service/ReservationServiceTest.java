package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.school.model.CreateReservationRequest;
import hei.student.school.model.Reservation;
import hei.student.school.model.ReservationStatus;
import hei.student.school.model.User;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JReservationRepository;
import hei.student.school.repository.JSeatRepository;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.mapper.JReservationMapper;
import hei.student.school.repository.model.JProjection;
import hei.student.school.repository.model.JReservation;
import hei.student.school.repository.model.JSeat;
import hei.student.school.repository.model.JUser;
import hei.student.school.security.model.Principal;
import hei.student.school.service.validator.CreateReservationValidator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  private static final UUID ID = UUID.randomUUID();
  private static final UUID PROJECTION_ID = UUID.randomUUID();

  @Mock JReservationRepository jReservationRepository;
  @Mock JUserRepository jUserRepository;
  @Mock JProjectionRepository jProjectionRepository;
  @Mock JSeatRepository jSeatRepository;
  @Mock JReservationMapper jReservationMapper;
  @Mock CreateReservationValidator createReservationValidator;
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
  void create_saves_pending_reservation_for_authenticated_client() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var client = new JUser();
    client.setId(clientId);
    var projection = new JProjection();
    var seat1 = seat();
    var seat2 = seat();
    var request =
        new CreateReservationRequest(PROJECTION_ID, List.of(seat1.getId(), seat2.getId()));
    var saved = new JReservation();
    var domain = new Reservation();
    when(jUserRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(jProjectionRepository.findById(PROJECTION_ID)).thenReturn(Optional.of(projection));
    when(jSeatRepository.findAllById(List.of(seat1.getId(), seat2.getId())))
        .thenReturn(List.of(seat1, seat2));
    when(jReservationRepository.findByProjectionId(PROJECTION_ID)).thenReturn(List.of());
    when(jReservationRepository.save(any(JReservation.class))).thenReturn(saved);
    when(jReservationMapper.toDomain(saved)).thenReturn(domain);

    var result = service.create(request);

    assertEquals(domain, result);
    var captor = ArgumentCaptor.forClass(JReservation.class);
    verify(jReservationRepository).save(captor.capture());
    var toSave = captor.getValue();
    assertEquals(ReservationStatus.PENDING, toSave.getStatus());
    assertNotNull(toSave.getCreatedAt());
    assertEquals(client, toSave.getClient());
    assertEquals(projection, toSave.getProjection());
    assertEquals(Set.of(seat1, seat2), toSave.getSeats());
  }

  @Test
  void create_throws_when_projection_not_found() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var client = new JUser();
    client.setId(clientId);
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(UUID.randomUUID()));
    when(jUserRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(jProjectionRepository.findById(PROJECTION_ID)).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class, () -> service.create(request));

    assertEquals("Projection with id " + PROJECTION_ID + " not found", exception.getMessage());
  }

  @Test
  void create_throws_when_seat_not_found() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var client = new JUser();
    client.setId(clientId);
    var missingSeatId = UUID.randomUUID();
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(missingSeatId));
    when(jUserRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(jProjectionRepository.findById(PROJECTION_ID)).thenReturn(Optional.of(new JProjection()));
    when(jSeatRepository.findAllById(List.of(missingSeatId))).thenReturn(List.of());

    var exception = assertThrows(IllegalArgumentException.class, () -> service.create(request));

    assertEquals("Seat(s) not found", exception.getMessage());
  }

  @Test
  void create_throws_when_seat_already_reserved() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var client = new JUser();
    client.setId(clientId);
    var takenSeat = seat();
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(takenSeat.getId()));
    var existing = new JReservation();
    existing.setStatus(ReservationStatus.SUCCESS);
    existing.setSeats(Set.of(takenSeat));
    when(jUserRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(jProjectionRepository.findById(PROJECTION_ID)).thenReturn(Optional.of(new JProjection()));
    when(jSeatRepository.findAllById(List.of(takenSeat.getId()))).thenReturn(List.of(takenSeat));
    when(jReservationRepository.findByProjectionId(PROJECTION_ID)).thenReturn(List.of(existing));

    var exception = assertThrows(IllegalArgumentException.class, () -> service.create(request));

    assertEquals("Seat(s) [" + takenSeat.getId() + "] already reserved", exception.getMessage());
  }

  @Test
  void create_allows_rebooking_canceled_seat() {
    var clientId = authenticateAs(UserRole.CLIENT);
    var client = new JUser();
    client.setId(clientId);
    var seat = seat();
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(seat.getId()));
    var canceled = new JReservation();
    canceled.setStatus(ReservationStatus.CANCELED);
    canceled.setSeats(Set.of(seat));
    var saved = new JReservation();
    var domain = new Reservation();
    when(jUserRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(jProjectionRepository.findById(PROJECTION_ID)).thenReturn(Optional.of(new JProjection()));
    when(jSeatRepository.findAllById(List.of(seat.getId()))).thenReturn(List.of(seat));
    when(jReservationRepository.findByProjectionId(PROJECTION_ID)).thenReturn(List.of(canceled));
    when(jReservationRepository.save(any(JReservation.class))).thenReturn(saved);
    when(jReservationMapper.toDomain(saved)).thenReturn(domain);

    var result = service.create(request);

    assertEquals(domain, result);
  }

  @Test
  void create_throws_access_denied_without_authentication() {
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(UUID.randomUUID()));

    assertThrows(AccessDeniedException.class, () -> service.create(request));
  }

  @Test
  void create_throws_access_denied_when_principal_is_not_principal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null));
    var request = new CreateReservationRequest(PROJECTION_ID, List.of(UUID.randomUUID()));

    assertThrows(AccessDeniedException.class, () -> service.create(request));
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

  private static JSeat seat() {
    var seat = new JSeat();
    seat.setId(UUID.randomUUID());
    return seat;
  }

  private static UUID authenticateAs(UserRole role) {
    return authenticateAs(role, UUID.randomUUID());
  }

  private static UUID authenticateAs(UserRole role, UUID id) {
    var user = new User();
    user.setId(id);
    user.setEmail("user@cinema.test");
    user.setRole(role);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(new Principal(user), null));
    return id;
  }
}
