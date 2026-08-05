package hei.student.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import hei.student.school.model.Projection;
import hei.student.school.repository.JProjectionRepository;
import hei.student.school.repository.mapper.JProjectionMapper;
import hei.student.school.repository.model.JProjection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

  @Mock JProjectionRepository jProjectionRepository;
  @Mock JProjectionMapper jProjectionMapper;
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
}
