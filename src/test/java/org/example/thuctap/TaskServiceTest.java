package org.example.thuctap;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.TaskRepository;
import org.example.thuctap.Service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllTasks_returnsList() {
        Task t1 = Task.builder().id(1L).title("A").description("d").status("PENDING").build();
        Task t2 = Task.builder().id(2L).title("B").description("d2").status("DONE").build();
        when(taskRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Task> all = taskService.getAllTasks();
        assertThat(all).hasSize(2);
        verify(taskRepository).findAll();
    }

    @Test
    void getTasksByUserId_callsRepository() {
        Long userId = 5L;
        Task t = Task.builder().id(1L).title("t").user(User.builder().id(userId).build()).build();
        when(taskRepository.findByUserId(userId)).thenReturn(Collections.singletonList(t));

        List<Task> res = taskService.getTasksByUserId(userId);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getUser().getId()).isEqualTo(userId);
        verify(taskRepository).findByUserId(userId);
    }

    @Test
    void createUpdateDelete_flow() {
        Task input = Task.builder().title("X").description("desc").status("PENDING").build();
        Task saved = Task.builder().id(10L).title("X").description("desc").status("PENDING").build();
        when(taskRepository.save(input)).thenReturn(saved);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(saved));

        Task s = taskService.createFrom(input);
        assertThat(s.getId()).isEqualTo(10L);
        verify(taskRepository).save(input);

        // updateTask should update fields
        Task details = Task.builder().title("X2").description("d2").status("DONE").user(null).build();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(saved));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Task updated = taskService.updateTask(10L, details);
        assertThat(updated.getTitle()).isEqualTo("X2");
        assertThat(updated.getStatus()).isEqualTo("DONE");
        verify(taskRepository, atLeastOnce()).findById(10L);

        // delete
        taskService.deleteTask(10L);
        verify(taskRepository).deleteById(10L);
    }

    @Test
    void paging_forAdmin_returnsPage() {
        Task t = Task.builder().id(1L).title("P").build();
        Page<Task> page = new PageImpl<>(Collections.singletonList(t));
        when(taskRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()))).thenReturn(page);

        Page<Task> result = taskService.getAllTasks(0,5,null,"createdAt","desc", true, 1L);
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(PageRequest.of(0,5, Sort.by("createdAt").descending()));
    }
}
