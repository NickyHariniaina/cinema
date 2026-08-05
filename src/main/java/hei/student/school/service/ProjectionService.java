package hei.student.school.service;

import hei.student.school.model.Projection;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.mapper.JProjectionMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectionService {
  private final JProjectionRepository jProjectionRepository;
  private final JProjectionMapper jProjectionMapper;

  public List<Projection> findAll() {
    return jProjectionRepository.findAll().stream().map(jProjectionMapper::toDomain).toList();
  }
}
