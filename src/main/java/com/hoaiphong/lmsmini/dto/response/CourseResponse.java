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
public class CourseResponse {
    private Long id;
    private String name;
    private String code;

    private List<ImageResponse> images;

    private Integer totalEnrollments;
    private Integer totalLessons;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
