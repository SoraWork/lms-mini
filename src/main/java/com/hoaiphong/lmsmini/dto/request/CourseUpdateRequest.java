package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {

    @NotBlank(message = "error.course.name.blank")
    @Size(min = 3, max = 150, message = "error.course.name.size")
    private String name;

    @NotBlank(message = "error.course.code.blank")
    @Size(min = 3, max = 20, message = "error.course.code.size")
    private String code;

    private List<Long> deleteImageIds;
}