package org.example.thuctap.Controller;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Service.TaskService;
import org.example.thuctap.dto.*;
import org.example.thuctap.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    // List: admin => all, user => own
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAll(Authentication auth) {
        logger.info("GET /api/tasks by {}", auth.getName());
        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));
        List<Task> tasks;
        if (isAdmin) tasks = taskService.getAllTasks();
        else {
            User user = userRepository.findByUsername(auth.getName());
            if (user == null) throw new UnauthorizedException("User not found");
            tasks = taskService.getTasksByUserId(user.getId());
        }
        List<TaskResponse> res = tasks.stream().map(TaskResponse::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(res, "Danh sách tasks"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable Long id, Authentication auth) {
        logger.info("GET /api/tasks/{} by {}", id, auth.getName());
        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));
        if (isAdmin) {
            Task t = taskService.getTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));
            return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(t), "OK"));
        } else {
            User user = userRepository.findByUsername(auth.getName());
            if (user == null) throw new UnauthorizedException("User not found");
            Task t = taskService.getTaskByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại hoặc không thuộc về bạn"));
            return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(t), "OK"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest req, BindingResult br, Authentication auth) {
        logger.info("POST /api/tasks by {}", auth.getName());
        if (br.hasErrors()) throw new BadRequestException("Dữ liệu không hợp lệ");

        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        Task t = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus() == null ? "PENDING" : req.getStatus())
                .user(user)
                .build();

        Task saved = taskService.createFrom(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(TaskResponse.fromEntity(saved), "Task tạo thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@PathVariable Long id,
                                                                @Valid @RequestBody TaskRequest req,
                                                                BindingResult br,
                                                                Authentication auth) {
        logger.info("PUT /api/tasks/{} by {}", id, auth.getName());
        if (br.hasErrors()) throw new BadRequestException("Dữ liệu không hợp lệ");

        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        if (!isAdmin) {
            // xác nhận task thuộc user
            Task existing = taskService.getTaskByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại hoặc không phải của bạn"));
            // gán lại owner, giữ nguyên id
            Task updated = Task.builder()
                    .id(existing.getId())
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .status(req.getStatus() == null ? existing.getStatus() : req.getStatus())
                    .user(user)
                    .build();
            Task saved = taskService.updateTask(id, updated);
            return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(saved), "Cập nhật thành công"));
        } else {
            // admin: can update any; if frontend không gởi owner -> giữ nguyên
            Task existing = taskService.getTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));
            Task toSave = Task.builder()
                    .id(existing.getId())
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .status(req.getStatus() == null ? existing.getStatus() : req.getStatus())
                    .user(existing.getUser()) // keep owner unless client passes new owner (we don't allow client to set owner here)
                    .build();
            Task saved = taskService.updateTask(id, toSave);
            return ResponseEntity.ok(ApiResponse.ok(TaskResponse.fromEntity(saved), "Cập nhật thành công (admin)"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication auth) {
        logger.info("DELETE /api/tasks/{} by {}", id, auth.getName());
        boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r -> r.equals("ADMIN"));
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) throw new UnauthorizedException("User not found");

        if (!isAdmin) {
            taskService.getTaskByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại hoặc không thuộc về bạn"));
        } else {
            taskService.getTaskById(id).orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

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

        // map sang DTO
        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(taskResponses, "OK", taskPage.getTotalElements(), taskPage.getTotalPages()));
    }

}
