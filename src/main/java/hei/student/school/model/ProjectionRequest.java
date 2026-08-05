package hei.student.school.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectionRequest(
    UUID movieId, UUID roomId, Instant datetime, BigDecimal seatPrice) {}
