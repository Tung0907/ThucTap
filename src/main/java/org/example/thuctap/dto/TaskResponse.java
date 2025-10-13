package org.example.thuctap.dto;

import org.example.thuctap.Model.Task;
import org.example.thuctap.Model.User;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private Long userId;
    private String userName;
    private String userFullName;

    public TaskResponse() {}

    public static TaskResponse fromEntity(Task t) {
        TaskResponse r = new TaskResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus());
        User u = t.getUser();
        if (u != null) {
            r.setUserId(u.getId());
            r.setUserName(u.getUsername());
            r.setUserFullName(u.getFullName());
        }
        return r;
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
}
