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
  private final JMovieRepository repository;
  private final JMovieMapper mapper;
  private final UpsertMovieValidator validator;

  public List<Movie> findAll() {
    return repository.findAll().stream()
        .map(mapper::toDomain)
        .sorted(Comparator.comparing(Movie::getTitle))
        .toList();
  }

  public Movie save(MovieRequest request) {
    validator.accept(request);

    var existing = repository.findByTitle(request.title()).orElse(null);
    var movie = new Movie();
    if (existing != null) {
      movie.setId(existing.getId());
    }
    movie.setTitle(request.title());
    movie.setGenres(request.genres());
    movie.setDescription(request.description());
    movie.setDuration(request.duration());

    return mapper.toDomain(repository.save(mapper.toEntity(movie)));
  }
}
