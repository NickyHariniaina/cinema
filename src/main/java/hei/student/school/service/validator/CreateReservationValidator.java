package hei.student.school.service.validator;

import hei.student.school.model.CreateReservationRequest;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class CreateReservationValidator implements Consumer<CreateReservationRequest> {
  @Override
  public void accept(CreateReservationRequest request) {
    var sj = new StringJoiner(". ");

    if (request.projectionId() == null) {
      sj.add("ProjectionId is mandatory");
    }
    if (request.seatIds() == null || request.seatIds().isEmpty()) {
      sj.add("At least one seat is required");
    }

    if (sj.length() > 0) {
      throw new IllegalArgumentException(sj.toString());
    }
  }
}
