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

    @NotBlank(message = "Tên khóa học không được để trống")
    private String name;

    @NotBlank(message = "Mã khóa học không được để trống")
    private String code;

    // ảnh cũ muốn xóa
    private List<Long> deleteImageIds;
}