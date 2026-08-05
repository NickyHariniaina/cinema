package hei.student.school.service.validator;

import hei.student.school.model.ProjectionRequest;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class CreateProjectionValidator implements Consumer<ProjectionRequest> {
  @Override
  public void accept(ProjectionRequest request) {
    var sj = new StringJoiner(". ");

    if (request.movieId() == null) {
      sj.add("MovieId is mandatory");
    }
    if (request.roomId() == null) {
      sj.add("RoomId is mandatory");
    }
    if (request.datetime() == null) {
      sj.add("Datetime is mandatory");
    }
    if (request.seatPrice() == null) {
      sj.add("SeatPrice is mandatory");
    }

    if (sj.length() > 0) {
      throw new IllegalArgumentException(sj.toString());
    }
  }
}
