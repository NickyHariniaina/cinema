package hei.student.school.model;

import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(UUID projectionId, List<UUID> seatIds) {}
