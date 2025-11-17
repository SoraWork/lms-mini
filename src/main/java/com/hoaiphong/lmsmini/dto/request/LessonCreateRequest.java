package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class LessonCreateRequest {

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải từ 3 đến 255 ký tự")
    private String title;

    @NotNull(message = "CourseId không được để trống")
    @Positive(message = "CourseId phải > 0")
    private Long courseId;

    @NotNull(message = "Danh sách ảnh không được null")
    private List<@NotNull Long> imageIds;
}