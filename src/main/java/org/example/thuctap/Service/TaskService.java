package org.example.thuctap.Service;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setStatus(taskDetails.getStatus());

            // Chỉ set user nếu có (tránh null xóa user cũ)
            if (taskDetails.getUser() != null) {
                task.setUser(taskDetails.getUser());
            }

            return taskRepository.save(task);
        }).orElse(null);
    }


    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
