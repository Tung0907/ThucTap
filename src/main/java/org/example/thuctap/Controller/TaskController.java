package org.example.thuctap.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Security.JwtUtil;
import org.example.thuctap.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // List:
    // - ADMIN -> all tasks
    // - USER -> only their tasks
    @GetMapping
    public ResponseEntity<?> getAllTasks(Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(taskService.getAllTasks());
        } else {
            User user = userRepository.findByUsername(username);
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            return ResponseEntity.ok(taskService.getTasksByUserId(user.getId()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));

        if (isAdmin) {
            Optional<Task> opt = taskService.getTaskById(id);
            return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            User user = userRepository.findByUsername(username);
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            Optional<Task> opt = taskService.getTaskByIdAndUserId(id, user.getId());
            return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
    }

    @PostMapping
    public ResponseEntity<?> addTask(@RequestBody Task task, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        // Gắn user từ token đăng nhập
        task.setUser(user);
        // Tránh lỗi nếu frontend gửi user = null
        Task saved = taskService.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task taskDetails, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ADMIN"));
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!isAdmin) {
            // user chỉ được cập nhật task của chính họ
            Optional<Task> opt = taskService.getTaskByIdAndUserId(id, user.getId());
            if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            taskDetails.setUser(user); // luôn giữ lại user hiện tại
            Task updated = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updated);

        } else {
            // ✅ admin cập nhật bất kỳ task nào
            // Nếu frontend không gửi user -> giữ nguyên user cũ
            Optional<Task> existing = taskService.getTaskById(id);
            if (existing.isPresent()) {
                if (taskDetails.getUser() == null) {
                    taskDetails.setUser(existing.get().getUser());
                }
            }
            Task updated = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updated);
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));
        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!isAdmin) {
            Optional<Task> opt = taskService.getTaskByIdAndUserId(id, user.getId());
            if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } else {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        }
    }
}
