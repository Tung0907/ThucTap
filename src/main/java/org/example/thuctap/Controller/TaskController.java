package org.example.thuctap.Controller;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Service.TaskService;
import org.example.thuctap.dto.*;
import org.example.thuctap.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/paging")
    public ResponseEntity<?> getTasksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        Page<Task> taskPage = taskService.getAllTasks(page, size, status, sortBy, direction, isAdmin, user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("content", taskPage.getContent().stream().map(TaskResponse::fromEntity).toList());
        data.put("totalPages", taskPage.getTotalPages());
        data.put("number", taskPage.getNumber());

        return ResponseEntity.ok(ApiResponse.ok(data, "OK"));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long id,
            Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        Task task = isAdmin
                ? taskService.getTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"))
                : taskService.getTaskByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại hoặc không thuộc bạn"));

        return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(task), "Lấy chi tiết task thành công"));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest req,
            Authentication auth) {

        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        Task t = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(Optional.ofNullable(req.getStatus()).orElse("PENDING"))
                .deadline(req.getDeadline())
                .user(user)
                .build();

        Task saved = taskService.createFrom(t);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(TaskResponse.fromEntity(saved), "Tạo Task thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest req,
            Authentication auth) {

        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        Task existing = isAdmin
                ? taskService.getTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"))
                : taskService.getTaskByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại hoặc không thuộc bạn"));

        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setStatus(req.getStatus());
        existing.setDeadline(req.getDeadline());

        Task updated = taskService.updateTask(id, existing);
        return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(updated), "Cập nhật thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        if (!isAdmin)
            taskService.getTaskByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tồn tại hoặc không thuộc bạn"));
        else
            taskService.getTaskById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
