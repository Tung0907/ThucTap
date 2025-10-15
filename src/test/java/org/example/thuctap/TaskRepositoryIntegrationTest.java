package org.example.thuctap;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.TaskRepository;
import org.example.thuctap.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByUserId() {
        User u = User.builder().username("tuser").password("p").fullName("T").email("t@x").role("USER").build();
        u = userRepository.save(u);

        Task t1 = Task.builder().title("A").description("d").status("PENDING").user(u).build();
        Task t2 = Task.builder().title("B").description("d2").status("DONE").user(u).build();
        taskRepository.saveAll(List.of(t1, t2));

        List<Task> found = taskRepository.findByUserId(u.getId());
        assertThat(found).hasSize(2);
    }
}
