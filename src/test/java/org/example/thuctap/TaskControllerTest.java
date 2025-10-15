package org.example.thuctap;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Service.TaskService;
import org.example.thuctap.dto.TaskRequest;
import org.example.thuctap.dto.TaskResponse;
import org.example.thuctap.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private org.example.thuctap.Controller.TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Authentication makeAuth(String username, String role) {
        return new Authentication() {
            @Override public Collection<SimpleGrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority(role)); }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return username; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
            @Override public String getName() { return username; }
        };
    }

    @Test
    void getById_asAdmin_returnsTask() {
        Authentication auth = makeAuth("admin", "ADMIN");
        User admin = User.builder().id(1L).username("admin").role("ADMIN").build();
        when(userRepository.findByUsername("admin")).thenReturn(admin);

        Task t = Task.builder().id(11L).title("T").description("D").status("DONE").build();
        when(taskService.getTaskById(11L)).thenReturn(Optional.of(t));

        ResponseEntity<?> resp = taskController.getTaskById(11L, auth);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        var body = (org.example.thuctap.dto.ApiResponse) resp.getBody();
        assertThat(body.getData()).isNotNull();
    }


    @Test
    void updateTask_nonAdmin_notOwner_forbidden() {
        Authentication auth = makeAuth("user1","USER");
        User u = User.builder().id(5L).username("user1").build();
        when(userRepository.findByUsername("user1")).thenReturn(u);
        when(taskService.getTaskByIdAndUserId(100L, 5L)).thenReturn(Optional.empty());

        TaskRequest req = new TaskRequest();
        req.setTitle("X");
        req.setDescription("d");
        req.setStatus("DONE");

        org.example.thuctap.exception.ResourceNotFoundException ex = null;
        try {
            taskController.updateTask(100L, req, auth);
        } catch (ResourceNotFoundException e) {
            ex = e;
        }
        assertThat(ex).isNotNull();
    }

    @Test
    void createTask_setsUserFromAuth() {
        Authentication auth = makeAuth("bob","USER");
        User u = User.builder().id(7L).username("bob").build();
        when(userRepository.findByUsername("bob")).thenReturn(u);

        TaskRequest req = new TaskRequest();
        req.setTitle("hello");
        req.setDescription("desc");
        req.setStatus("PENDING");

        Task saved = Task.builder().id(33L).title("hello").description("desc").status("PENDING").user(u).build();
        when(taskService.createFrom(any(Task.class))).thenReturn(saved);

        ResponseEntity<?> resp = taskController.createTask(req,auth);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        var api = (org.example.thuctap.dto.ApiResponse) resp.getBody();
        TaskResponse tr = (TaskResponse) api.getData();
        assertThat(tr.getId()).isEqualTo(33L);
    }
}
