package com.hoaiphong.lmsmini.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private Long id;
    private String title;
    private Long courseId;
    private List<Long> imageIds;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
