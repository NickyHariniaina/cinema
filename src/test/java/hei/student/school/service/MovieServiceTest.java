package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.school.model.Genre;
import hei.student.school.model.Movie;
import hei.student.school.model.MovieRequest;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.mapper.JMovieMapper;
import hei.student.school.repository.model.JMovie;
import hei.student.school.service.validator.UpsertMovieValidator;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

  @Mock JMovieRepository jMovieRepository;
  @Mock JMovieMapper jMovieMapper;
  @Mock UpsertMovieValidator upsertMovieValidator;
  @InjectMocks MovieService movieService;

  @Test
  void findAll_returns_movies_sorted_by_title() {
    var zebra = jMovie("Zebra", Set.of(), Duration.ofHours(1));
    var avatar = jMovie("Avatar", Set.of(Genre.SCI_FI), Duration.ofHours(2));
    var mZebra = domainOf(zebra);
    var mAvatar = domainOf(avatar);
    when(jMovieRepository.findAll()).thenReturn(List.of(zebra, avatar));
    when(jMovieMapper.toDomain(zebra)).thenReturn(mZebra);
    when(jMovieMapper.toDomain(avatar)).thenReturn(mAvatar);

    var result = movieService.findAll();

    assertEquals(List.of(mAvatar, mZebra), result);
  }

  @Test
  void findAll_returns_empty_when_no_movie() {
    when(jMovieRepository.findAll()).thenReturn(List.of());

    assertTrue(movieService.findAll().isEmpty());
  }

  @Test
  void save_creates_new_movie_when_title_does_not_exist() {
    var request = new MovieRequest("Dune", Set.of(Genre.SCI_FI), "Desert planet", Duration.ofHours(2));
    when(jMovieRepository.findByTitle("Dune")).thenReturn(Optional.empty());
    var entityToSave = new JMovie(null, "Dune", Set.of(Genre.SCI_FI), "Desert planet", Duration.ofHours(2));
    var saved = jMovie("Dune", Set.of(Genre.SCI_FI), Duration.ofHours(2));
    saved.setDescription("Desert planet");
    var savedDomain = domainOf(saved);
    when(jMovieMapper.toEntity(any())).thenReturn(entityToSave);
    when(jMovieRepository.save(entityToSave)).thenReturn(saved);
    when(jMovieMapper.toDomain(saved)).thenReturn(savedDomain);

    var result = movieService.save(request);

    assertEquals(savedDomain, result);
    var captor = ArgumentCaptor.forClass(Movie.class);
    verify(jMovieMapper).toEntity(captor.capture());
    assertNull(captor.getValue().getId());
    verify(upsertMovieValidator).accept(request);
  }

  @Test
  void save_updates_existing_movie_when_title_exists() {
    var existing = jMovie("Dune", Set.of(Genre.SCI_FI), Duration.ofHours(2));
    existing.setDescription("Old synopsis");
    var request = new MovieRequest("Dune", Set.of(Genre.SCI_FI, Genre.DRAMA), "New synopsis", Duration.ofHours(3));
    when(jMovieRepository.findByTitle("Dune")).thenReturn(Optional.of(existing));
    var entityToSave = new JMovie(existing.getId(), "Dune", Set.of(Genre.SCI_FI, Genre.DRAMA), "New synopsis", Duration.ofHours(3));
    var saved = new JMovie(existing.getId(), "Dune", Set.of(Genre.SCI_FI, Genre.DRAMA), "New synopsis", Duration.ofHours(3));
    var savedDomain = domainOf(saved);
    when(jMovieMapper.toEntity(any())).thenReturn(entityToSave);
    when(jMovieRepository.save(entityToSave)).thenReturn(saved);
    when(jMovieMapper.toDomain(saved)).thenReturn(savedDomain);

    var result = movieService.save(request);

    assertEquals(savedDomain, result);
    var captor = ArgumentCaptor.forClass(Movie.class);
    verify(jMovieMapper).toEntity(captor.capture());
    assertEquals(existing.getId(), captor.getValue().getId());
    assertEquals(Set.of(Genre.SCI_FI, Genre.DRAMA), captor.getValue().getGenres());
    assertEquals("New synopsis", captor.getValue().getDescription());
    assertEquals(Duration.ofHours(3), captor.getValue().getDuration());
  }

  @Test
  void save_propagates_validation_error() {
    var request = new MovieRequest(null, null, null, null);
    doThrow(new IllegalArgumentException("Title is mandatory"))
        .when(upsertMovieValidator)
        .accept(request);

    assertThrows(IllegalArgumentException.class, () -> movieService.save(request));

    verify(jMovieRepository, never()).findByTitle(any());
    verify(jMovieRepository, never()).save(any());
  }

  private static JMovie jMovie(String title, Set<Genre> genres, Duration duration) {
    return new JMovie(UUID.randomUUID(), title, genres, title + " synopsis", duration);
  }

  private static Movie domainOf(JMovie entity) {
    var movie = new Movie();
    movie.setId(entity.getId());
    movie.setTitle(entity.getTitle());
    movie.setGenres(entity.getGenres());
    movie.setDescription(entity.getDescription());
    movie.setDuration(entity.getDuration());
    return movie;
  }
}
