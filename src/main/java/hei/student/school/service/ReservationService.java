package hei.student.school.service;

import hei.student.school.model.Reservation;
import hei.student.school.model.UserRole;
import hei.student.school.repository.JReservationRepository;
import hei.student.school.repository.mapper.JReservationMapper;
import hei.student.school.security.model.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReservationService {
  private final JReservationRepository jReservationRepository;
  private final JReservationMapper jReservationMapper;

  @Transactional(readOnly = true)
  public List<Reservation> findAll() {
    return jReservationRepository.findAll().stream().map(jReservationMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public Reservation findById(UUID id) {
    var reservation =
        jReservationRepository
            .findById(id)
            .orElseThrow(
                () -> new NoSuchElementException("Reservation with id " + id + " not found"));

    assertCanView(reservation.getClient() == null ? null : reservation.getClient().getId());
    return jReservationMapper.toDomain(reservation);
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
