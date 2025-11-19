package com.hoaiphong.lmsmini.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LessonUpdateRequest {

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(min = 3, max = 255, message = "Tiêu đề phải từ 3 đến 255 ký tự")
    private String title;

    private List<Long> imageIds; // danh sách ảnh bị update status = 0

    private List<Long> videoIds; // danh sách vid bị update status = 0
}
