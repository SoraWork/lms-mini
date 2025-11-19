package com.hoaiphong.lmsmini.dto.response;

import com.hoaiphong.lmsmini.base.PageResponse;
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
public class CourseStudentResponse {

    private Long id;
    private String name;
    private String code;

    private List<ImageResponse> images;

    // Danh sách student trong course
    private PageResponse<StudentInCourseResponse> students;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
