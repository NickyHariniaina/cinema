package hei.student.school.repository;

import hei.student.school.repository.model.JMovie;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JMovieRepository extends JpaRepository<JMovie, UUID> {
  Optional<JMovie> findByTitle(String title);
}
