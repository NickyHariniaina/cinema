package hei.student.school.endpoint.rest.controller;

import hei.student.school.model.CreateReservationRequest;
import hei.student.school.model.Reservation;
import hei.student.school.service.ReservationService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping()
public class ReservationController {
  private final ReservationService service;

  @GetMapping("/reservations")
  public List<Reservation> listReservations() {
    return service.findAll();
  }

  @PutMapping("/reservations")
  public Reservation createReservation(@RequestBody CreateReservationRequest request) {
    return service.create(request);
  }

  @GetMapping("/reservations/{id}")
  public Reservation getReservationById(@PathVariable UUID id) {
    return service.findById(id);
  }
}
