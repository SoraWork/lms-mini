package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCreateRequest {

    @NotNull(message = "StudentId không được để trống")
    @Positive(message = "StudentId phải > 0")
    private Long studentId;

    @NotNull(message = "CourseId không được để trống")
    @Positive(message = "CourseId phải > 0")
    private Long courseId;
}
