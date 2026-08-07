package hei.student.school.service;

import hei.student.school.model.Projection;
import hei.student.school.model.ProjectionRequest;
import hei.student.school.repository.JMovieRepository;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.JRoomRepository;
import hei.student.school.repository.mapper.JProjectionMapper;
import hei.student.school.repository.model.JProjection;
import hei.student.school.service.validator.CreateProjectionValidator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProjectionService {
  private final JProjectionRepository jProjectionRepository;
  private final JMovieRepository jMovieRepository;
  private final JRoomRepository jRoomRepository;
  private final JProjectionMapper mapper;
  private final CreateProjectionValidator validator;

  public List<Projection> findAll() {
    return jProjectionRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Transactional
  public Projection create(ProjectionRequest request) {
    validator.accept(request);

    var movie =
        jMovieRepository
            .findById(request.movieId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Movie with id " + request.movieId() + " not found"));
    var room =
        jRoomRepository
            .findById(request.roomId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Room with id " + request.roomId() + " not found"));

    var projection = new JProjection(null, movie, room, request.datetime(), request.seatPrice());
    return mapper.toDomain(jProjectionRepository.save(projection));
  }
}
