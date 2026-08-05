package hei.student.school.repository;

import hei.student.school.repository.model.JSeat;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JSeatRepository extends JpaRepository<JSeat, UUID> {}
