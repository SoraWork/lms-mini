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

    @NotBlank(message = "error.lesson.item.title.blank")
    @Size(min = 3, max = 255, message = "error.lesson.item.title.size")
    private String title;

    private List<Long> imageIds; // danh sách ảnh bị update status = 0

    private List<Long> videoIds; // danh sách vid bị update status = 0
}
