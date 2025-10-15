package org.example.thuctap.dto;

import lombok.*;
import org.example.thuctap.Model.Task;

import java.time.*;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String userFullName;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;

    public static TaskResponse fromEntity(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .userFullName(t.getUser() != null ? t.getUser().getFullName() : "N/A")
                .createdAt(convertDate(t.getCreatedAt()))
                .deadline(convertDate(t.getDeadline()))
                .build();
    }

    private static LocalDateTime convertDate(java.util.Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
