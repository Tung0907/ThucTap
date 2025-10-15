package org.example.thuctap.Service;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.*;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> getTaskByIdAndUserId(Long id, Long userId) {
        return taskRepository.findByIdAndUser_Id(id, userId);
    }

    public Task addTask(Task task) {
        return taskRepository.save(task);
    }
    public Task createFrom(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setStatus(taskDetails.getStatus());

            //  Giữ nguyên user cũ nếu frontend không gửi user mới
            if (taskDetails.getUser() != null) {
                task.setUser(taskDetails.getUser());
            }

            return taskRepository.save(task);
        }).orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }



    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Page<Task> getAllTasks(int page, int size, String status, String sortBy, String direction, boolean isAdmin, Long userId) {
        Sort sort = direction.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (isAdmin) {
            if (status != null && !status.isEmpty())
                return taskRepository.findByStatus(status, pageable);
            else
                return taskRepository.findAll(pageable);
        } else {
            if (status != null && !status.isEmpty())
                return taskRepository.findByUserIdAndStatus(userId, status, pageable);
            else
                return taskRepository.findByUserId(userId, pageable);
        }
    }
}
