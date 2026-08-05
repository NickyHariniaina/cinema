package hei.student.school.service;

import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.mapper.JMovieMapper;
import hei.student.school.service.validator.UpsertMovieValidator;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {
  private final JMovieRepository jMovieRepository;
  private final JMovieMapper jMovieMapper;
  private final UpsertMovieValidator upsertMovieValidator;

  public List<Movie> findAll() {
    return jMovieRepository.findAll().stream()
        .map(jMovieMapper::toDomain)
        .sorted(Comparator.comparing(Movie::getTitle))
        .toList();
  }

  public Movie save(MovieRequest request) {
    upsertMovieValidator.accept(request);

    var existing = jMovieRepository.findByTitle(request.title()).orElse(null);
    var movie = new Movie();
    if (existing != null) {
      movie.setId(existing.getId());
    }
    movie.setTitle(request.title());
    movie.setGenres(request.genres());
    movie.setDescription(request.description());
    movie.setDuration(request.duration());

    return jMovieMapper.toDomain(jMovieRepository.save(jMovieMapper.toEntity(movie)));
  }
}
