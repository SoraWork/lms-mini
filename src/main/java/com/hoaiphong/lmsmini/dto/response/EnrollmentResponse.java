package com.hoaiphong.lmsmini.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long studentId;
    private Long courseId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
