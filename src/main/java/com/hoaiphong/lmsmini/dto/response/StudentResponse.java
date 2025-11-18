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
public class StudentResponse {
    private Long id;
    private String name;
    private String email;

    private List<ImageResponse> images;

    private List<CourseSummaryResponse> courses;

    private Integer totalEnrollments;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
