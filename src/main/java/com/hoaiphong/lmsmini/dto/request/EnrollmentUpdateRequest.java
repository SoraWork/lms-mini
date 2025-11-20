package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdateRequest {

    @NotNull(message = "error.enrollment.studentId.null")
    private Long studentId;

    private List<Long> courseIdsDelete;

    private List<Long> courseIdsCreate;
}
