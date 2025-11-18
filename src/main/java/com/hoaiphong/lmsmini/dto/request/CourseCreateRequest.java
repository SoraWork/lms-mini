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
public class CourseCreateRequest {

    @NotBlank(message = "Tên khóa học không được để trống")
    @Size(min = 3, max = 150, message = "Tên khóa học phải từ 3 đến 150 ký tự")
    private String name;

    @NotBlank(message = "Mã khóa học không được để trống")
    @Size(min = 3, max = 20, message = "Code phải từ 3–20 ký tự")
    private String code;

    private List<LessonCreateRequest> lessons;

}