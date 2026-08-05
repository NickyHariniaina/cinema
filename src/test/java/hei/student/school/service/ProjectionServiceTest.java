package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.student.school.model.Projection;
import hei.student.school.model.ProjectionRequest;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JRoomRepository;
import hei.student.school.repository.mapper.JProjectionMapper;
import hei.student.school.repository.model.JMovie;
import hei.student.school.repository.model.JProjection;
import hei.student.school.repository.model.JRoom;
import hei.student.school.service.validator.CreateProjectionValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

  private static final Instant DATETIME = Instant.parse("2026-08-06T20:00:00Z");
  private static final BigDecimal SEAT_PRICE = BigDecimal.TEN;

  @Mock JProjectionRepository jProjectionRepository;
  @Mock JMovieRepository jMovieRepository;
  @Mock JRoomRepository jRoomRepository;
  @Mock JProjectionMapper jProjectionMapper;
  @Mock CreateProjectionValidator createProjectionValidator;
  @InjectMocks ProjectionService service;

  @Test
  void findAll_maps_all_projections_to_domain() {
    var entity = new JProjection();
    var domain = new Projection();
    when(jProjectionRepository.findAll()).thenReturn(List.of(entity));
    when(jProjectionMapper.toDomain(entity)).thenReturn(domain);

    var result = service.findAll();

    assertEquals(List.of(domain), result);
  }

  @Test
  void create_saves_projection_and_returns_mapped_domain() {
    var movieId = UUID.randomUUID();
    var roomId = UUID.randomUUID();
    var movie = new JMovie();
    var room = new JRoom();
    var saved = new JProjection();
    var domain = new Projection();
    var request = new ProjectionRequest(movieId, roomId, DATETIME, SEAT_PRICE);
    when(jMovieRepository.findById(movieId)).thenReturn(Optional.of(movie));
    when(jRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
    when(jProjectionRepository.save(any(JProjection.class))).thenReturn(saved);
    when(jProjectionMapper.toDomain(saved)).thenReturn(domain);

    var result = service.create(request);

    assertEquals(domain, result);
    var captor = ArgumentCaptor.forClass(JProjection.class);
    verify(jProjectionRepository).save(captor.capture());
    var toSave = captor.getValue();
    assertNull(toSave.getId());
    assertEquals(movie, toSave.getMovie());
    assertEquals(room, toSave.getRoom());
    assertEquals(DATETIME, toSave.getDatetime());
    assertEquals(SEAT_PRICE, toSave.getSeatPrice());
  }

  @Test
  void create_throws_when_movie_not_found() {
    var movieId = UUID.randomUUID();
    var roomId = UUID.randomUUID();
    var request = new ProjectionRequest(movieId, roomId, DATETIME, SEAT_PRICE);
    when(jMovieRepository.findById(movieId)).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class, () -> service.create(request));

    assertEquals("Movie with id " + movieId + " not found", exception.getMessage());
  }

  @Test
  void create_throws_when_room_not_found() {
    var movieId = UUID.randomUUID();
    var roomId = UUID.randomUUID();
    var request = new ProjectionRequest(movieId, roomId, DATETIME, SEAT_PRICE);
    when(jMovieRepository.findById(movieId)).thenReturn(Optional.of(new JMovie()));
    when(jRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    var exception = assertThrows(IllegalArgumentException.class, () -> service.create(request));

    assertEquals("Room with id " + roomId + " not found", exception.getMessage());
  }

  @Test
  void create_propagates_validator_rejection() {
    var request = new ProjectionRequest(UUID.randomUUID(), UUID.randomUUID(), DATETIME, SEAT_PRICE);
    doThrow(new IllegalArgumentException("x")).when(createProjectionValidator).accept(request);

    assertThrows(IllegalArgumentException.class, () -> service.create(request));
  }
}
