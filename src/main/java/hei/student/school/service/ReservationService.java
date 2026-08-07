package hei.student.school.service;

import hei.student.school.model.CreateReservationRequest;
import hei.student.school.model.Reservation;
import hei.student.school.model.ReservationStatus;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JReservationRepository;
import hei.student.school.repository.JSeatRepository;
import hei.student.school.repository.JUserRepository;
import hei.student.school.repository.mapper.JReservationMapper;
import hei.student.school.repository.model.JReservation;
import hei.student.school.repository.model.JSeat;
import hei.student.school.security.model.Principal;
import hei.student.school.service.validator.CreateReservationValidator;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReservationService {
  private final JReservationRepository jReservationRepository;
  private final JUserRepository jUserRepository;
  private final JProjectionRepository jProjectionRepository;
  private final JSeatRepository jSeatRepository;
  private final JReservationMapper mapper;
  private final CreateReservationValidator validator;

  public List<Reservation> findAll() {
    return jReservationRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional
  public Reservation create(CreateReservationRequest request) {
    validator.accept(request);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Principal principal)) {
      throw new AccessDeniedException("Authentication required");
    }

    var client =
        jUserRepository
            .findById(principal.user().getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "User with id " + principal.user().getId() + " not found"));
    var projection =
        jProjectionRepository
            .findById(request.projectionId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Projection with id " + request.projectionId() + " not found"));

    var seats = jSeatRepository.findAllById(request.seatIds());
    if (seats.size() != new HashSet<>(request.seatIds()).size()) {
      throw new IllegalArgumentException("Seat(s) not found");
    }
    assertSeatsAvailable(request.projectionId(), request.seatIds());

    var reservation = new JReservation();
    reservation.setCreatedAt(Instant.now());
    reservation.setStatus(ReservationStatus.PENDING);
    reservation.setClient(client);
    reservation.setProjection(projection);
    reservation.setSeats(new HashSet<>(seats));

    return mapper.toDomain(jReservationRepository.save(reservation));
  }

  private void assertSeatsAvailable(UUID projectionId, List<UUID> requestedSeatIds) {
    var takenSeatIds =
        jReservationRepository.findByProjectionId(projectionId).stream()
            .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELED)
            .flatMap(reservation -> reservation.getSeats().stream())
            .map(JSeat::getId)
            .collect(Collectors.toSet());

    var unavailable = new HashSet<>(requestedSeatIds);
    unavailable.retainAll(takenSeatIds);
    if (!unavailable.isEmpty()) {
      throw new IllegalArgumentException("Seat(s) " + unavailable + " already reserved");
    }
  }

  @Transactional(readOnly = true)
  public Reservation findById(UUID id) {
    var reservation =
        jReservationRepository
            .findById(id)
            .orElseThrow(
                () -> new NoSuchElementException("Reservation with id " + id + " not found"));

    assertCanView(reservation.getClient() == null ? null : reservation.getClient().getId());
    return mapper.toDomain(reservation);
  }

  private void assertCanView(UUID reservationClientId) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Principal principal)) {
      throw new AccessDeniedException("Authentication required");
    }

    var currentUser = principal.user();
    if (currentUser.getRole() == UserRole.CLIENT
        && (reservationClientId == null || !reservationClientId.equals(currentUser.getId()))) {
      throw new AccessDeniedException("A CLIENT can only view its own reservations");
    }
  }
}
