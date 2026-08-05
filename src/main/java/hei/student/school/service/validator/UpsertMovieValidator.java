package hei.student.school.service.validator;

import hei.student.school.model.MovieRequest;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class UpsertMovieValidator implements Consumer<MovieRequest> {
  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  @Override
  public void accept(MovieRequest request) {
    var sj = new StringJoiner(". ");

    if (isBlank(request.title())) {
      sj.add("Title is mandatory");
    }
    if (request.genres() == null || request.genres().isEmpty()) {
      sj.add("Genres are mandatory");
    }
    if (isBlank(request.description())) {
      sj.add("Description is mandatory");
    }
    if (request.duration() == null) {
      sj.add("Duration is mandatory");
    }

    if (sj.length() > 0) {
      throw new IllegalArgumentException(sj.toString());
    }
  }
}
