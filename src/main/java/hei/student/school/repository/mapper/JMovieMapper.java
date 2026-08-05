package hei.student.school.repository.mapper;

import hei.student.school.model.Movie;
import hei.student.school.repository.model.JMovie;
import org.springframework.stereotype.Component;

@Component
public class JMovieMapper {
  public Movie toDomain(JMovie entity) {
    var movie = new Movie();
    movie.setId(entity.getId());
    movie.setTitle(entity.getTitle());
    movie.setGenres(entity.getGenres());
    movie.setDescription(entity.getDescription());
    movie.setDuration(entity.getDuration());
    return movie;
  }

  public JMovie toEntity(Movie movie) {
    return new JMovie(
        movie.getId(),
        movie.getTitle(),
        movie.getGenres(),
        movie.getDescription(),
        movie.getDuration());
  }
}
