package org.example.thuctap.Repository;
import org.example.thuctap.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    Optional<Task> findByIdAndUser_Id(Long id, Long userId);
    Page<Task> findByStatus(String status, Pageable pageable);
    Page<Task> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    Page<Task> findByUserId(Long userId, Pageable pageable);
}
